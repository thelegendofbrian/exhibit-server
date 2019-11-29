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
import minepop.exhibit.schedule.ScheduleDAO
import minepop.exhibit.schedule.calculateStatsUpdate
import minepop.exhibit.stats.StatsDAO
import java.sql.Date

private val scheduleDAO = ScheduleDAO()
private val groupDAO = GroupDAO()
private val checkinDAO = CheckinDAO()
private val statsDAO = StatsDAO()

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
        val lastScheduledCheckin = statsDAO.retrieveLastScheduledCheckin(groupMemberId)?.date
        val schedules = scheduleDAO.retrieveSchedules(groupMemberId, lastScheduledCheckin, dateNow)

        val stats = schedules.calculateStatsUpdate(groupMemberId, lastScheduledCheckin?.toLocalDate(), now)
        statsDAO.updateStats(stats)
        checkinDAO.createCheckin(groupMemberId, dateNow, stats.isBonusCheckin)

        val body = JsonObject()
        body.addProperty("date", dateNow.toString())
        call.respond(body)
    }
}