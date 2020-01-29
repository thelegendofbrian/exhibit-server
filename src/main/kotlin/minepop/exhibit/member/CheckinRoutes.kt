package minepop.exhibit.member

import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import minepop.exhibit.auth.currentDate
import minepop.exhibit.auth.exhibitSession
import minepop.exhibit.auth.now
import minepop.exhibit.checkin.CheckinDAO
import minepop.exhibit.group.GroupDAO
import minepop.exhibit.schedule.CheckinType
import minepop.exhibit.stats.updateGroupStats
import java.sql.Date

private val groupDAO = GroupDAO()
private val checkinDAO = CheckinDAO()

fun Route.checkinRoutes() {
    get("/") {
        val pastDays = call.request.queryParameters["pastDays"]?.toIntOrNull()
        val groupId = call.parameters["groupId"]!!.toLong()
        if (pastDays != null) {
            val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, exhibitSession().userId)!!
            val checkins = checkinDAO.retrieveGroupMemberCheckins(groupMemberId, exhibitSession().currentDate(), pastDays)
            if (pastDays == 1) {
                if (checkins.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(checkins[0])
                }
            } else {
                call.respond(checkins)
            }
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
    post("/") {
        val groupId = call.parameters["groupId"]!!.toLong()
        val userId = exhibitSession().userId
        val now = exhibitSession().now()
        val dateNow = Date.valueOf(now)

        val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!
        val stats = updateGroupStats(groupMemberId, true)
        if (stats == null) {
            // there is no good std http code for this scenario
            // 503 is closest, but it is not really a server error
            call.respond(HttpStatusCode.ServiceUnavailable)
            return@post
        }
        checkinDAO.createCheckin(groupMemberId, dateNow, stats.checkinType == CheckinType.BONUS)

        val body = JsonObject()
        body.addProperty("date", dateNow.toString())
        call.respond(body)
    }
}