package minepop.exhibit.member

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import minepop.exhibit.auth.currentDate
import minepop.exhibit.auth.exhibitSession
import minepop.exhibit.auth.now
import minepop.exhibit.group.GroupDAO
import minepop.exhibit.schedule.IntervalSchedule
import minepop.exhibit.schedule.Schedule
import minepop.exhibit.schedule.ScheduleDAO
import minepop.exhibit.schedule.WeeklySchedule
import java.sql.Date
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

private val scheduleDAO = ScheduleDAO()
private val groupDAO = GroupDAO()

fun Route.scheduleRoutes() {
    post("/") {
        val userId = exhibitSession().userId
        val groupId = call.parameters["groupId"]!!.toLong()
        val body = call.receive<JsonObject>()
        val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!

        val startDate = Date.valueOf(LocalDate.parse(body.get("startDate").asString))
        val scheduleType = body.get("type").asString
        var schedule: Schedule? = null
        if (scheduleType == "weekly") {
            schedule = WeeklySchedule(groupMemberId, startDate)
            body.get("days").asJsonArray.forEach {
                (schedule as WeeklySchedule).days += DayOfWeek.valueOf(it.asString.toUpperCase()).value
            }
        } else if (scheduleType == "interval") {
            schedule = IntervalSchedule(groupMemberId, startDate)
            schedule.days = body.get("days").asInt
        }
        scheduleDAO.createUpdateSchedule(schedule!!)
        call.respond(HttpStatusCode.NoContent)
    }

    get("/") {
        val userId = exhibitSession().userId
        val groupId = call.parameters["groupId"]!!.toLong()
        val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!
        val schedule = scheduleDAO.retrieveSchedule(groupMemberId, exhibitSession().currentDate())
        if (schedule == null) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            val body = JsonObject()
            body.addProperty("startDate", schedule.startDate.toString())
            body.addProperty("type", if (schedule is WeeklySchedule) "weekly" else "interval")
            if (schedule is WeeklySchedule) {
                val array = JsonArray()
                schedule.days.forEach {
                    val day = DayOfWeek.of(it).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    array.add(day)
                }
                body.add("days", array)
            } else if (schedule is IntervalSchedule) {
                body.addProperty("days", schedule.days)
            }
            call.respond(body)
        }
    }

    get("/projection") {
        val userId = exhibitSession().userId
        val groupId = call.parameters["groupId"]!!.toLong()
        val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!
        val schedule = scheduleDAO.retrieveSchedule(groupMemberId, exhibitSession().currentDate())
        if (schedule == null) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            val now = exhibitSession().now()
            val response = JsonObject()
            val projectedSchedule = JsonArray()
            schedule.iterate(iterateStart = now, iterateEnd = now.plusDays(7)) {
                projectedSchedule.add(it.dayOfWeek.value)
            }
            response.addProperty("type", if (schedule is WeeklySchedule) "weekly" else "interval")
            response.add("days", projectedSchedule)
            call.respond(response)
        }
    }
}