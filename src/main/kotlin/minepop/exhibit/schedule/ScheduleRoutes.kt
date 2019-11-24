package minepop.exhibit.schedule

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import minepop.exhibit.auth.currentDate
import minepop.exhibit.auth.exhibitSession
import minepop.exhibit.group.GroupDAO
import java.sql.Date
import java.time.DayOfWeek
import java.time.LocalDate

val groupDAO = GroupDAO()
val scheduleDAO = ScheduleDAO()

fun Route.scheduleRoutes() {
    route("schedule/{groupId}") {
        post("/") {
            val userId = exhibitSession().userid
            val groupId = call.parameters["groupId"]!!.toLong()
            val body = call.receive<JsonObject>()

            val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!
            val startDate = Date.valueOf(LocalDate.parse(body.get("startDate").asString))
            val scheduleType = body.get("type").asString
            var schedule: Schedule? = null
            if (scheduleType == "Weekly") {
                schedule = WeeklySchedule(groupMemberId, startDate)
                body.get("days").asJsonArray.forEach {
                    (schedule as WeeklySchedule).days += DayOfWeek.valueOf(it.asString).value
                }
            } else if (scheduleType == "Interval") {
                schedule = IntervalSchedule(groupMemberId, startDate)
                schedule.days = body.get("days").asInt
            }
            scheduleDAO.createUpdateSchedule(schedule!!)
            call.respond(HttpStatusCode.NoContent)
        }

        get("/") {
            val userId = exhibitSession().userid
            val groupId = call.parameters["groupId"]!!.toLong()
            val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!
            val schedule = scheduleDAO.retrieveSchedule(groupMemberId, exhibitSession().currentDate())
            if (schedule == null) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                val body = JsonObject()
                body.addProperty("startDate", schedule.startDate.toString())
                body.addProperty("scheduleType", if (schedule is WeeklySchedule) "Weekly" else "Interval")
                if (schedule is WeeklySchedule) {
                    val array = JsonArray()
                    schedule.days.forEach {
                        array.add(it)
                    }
                    body.add("days", array)
                } else if (schedule is IntervalSchedule) {
                    body.addProperty("days", schedule.days)
                }
                call.respond(body)
            }
        }
    }
}