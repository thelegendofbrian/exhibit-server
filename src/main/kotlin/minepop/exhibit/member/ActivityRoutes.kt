package minepop.exhibit.member

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import minepop.exhibit.auth.exhibitSession
import minepop.exhibit.checkin.CheckinDAO
import minepop.exhibit.group.GroupDAO
import minepop.exhibit.schedule.ScheduleDAO
import minepop.exhibit.schedule.iterate
import java.sql.Date
import java.time.LocalDate
import java.util.*

private val groupDAO = GroupDAO()
private val scheduleDAO = ScheduleDAO()
private val checkinDAO = CheckinDAO()

fun Route.activityRoutes() {
    get("/") {
        val startDate = call.request.queryParameters["startDate"]!!
        val numMonths = call.request.queryParameters["numMonths"]!!.toLong()
        val localStartDate = LocalDate.parse(startDate).withDayOfMonth(1)
        val localEndDate = localStartDate.plusMonths(numMonths)
        val groupId = call.parameters["groupId"]!!.toLong()
        val userId = exhibitSession().userId

        val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!
        val sqlStart = Date.valueOf(localStartDate)
        val sqlEnd = Date.valueOf(localEndDate)
        val schedules = scheduleDAO.retrieveSchedules(groupMemberId, sqlStart, sqlEnd)
        val checkins = checkinDAO.retrieveGroupMemberCheckins(groupMemberId, sqlStart, sqlEnd)

        val activityMap = TreeMap<LocalDate, JsonObject>()

        checkins.forEach {
            val checkinDate = it.date.toLocalDate()

            val activity = JsonObject()
            activity.addProperty("date", checkinDate.toString())
            activity.addProperty("type", if (it.isBonus) "bonus" else "scheduled")

            activityMap[checkinDate] = activity
        }

        val scheduledCheckins = activityMap.keys

        schedules.iterate(localStartDate, localEndDate) {
            if (!scheduledCheckins.contains(it)) {
                val activity = JsonObject()
                activity.addProperty("date", it.toString())
                activity.addProperty("type", "missed")
            }
        }

        val activityArray = JsonArray()
        activityMap.values.forEach {
            activityArray.add(it)
        }
        call.respond(activityArray)
    }
}