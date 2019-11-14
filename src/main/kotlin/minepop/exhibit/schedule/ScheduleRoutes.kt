package minepop.exhibit.schedule

import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import minepop.exhibit.ExhibitSession

val scheduleDAO = ScheduleDAO()

fun Route.scheduleRoutes() {
    route("schedule") {
        post("/") {
            val userName = call.sessions.get<ExhibitSession>()!!.username
            val groupName = "Japanese"
            val body = call.receive<JsonObject>()

            val scheduleType = body.get("type").asString
            var schedule: Schedule? = null
            if (scheduleType == "Weekly") {
                schedule = WeeklySchedule(userName, groupName)
                body.get("days").asJsonArray.forEach {
                    (schedule as WeeklySchedule).days += it.asString
                }
            } else if (scheduleType == "Interval") {
                schedule = IntervalSchedule(userName, groupName)
                schedule.days = body.get("days").asInt
            }
            scheduleDAO.createUpdateSchedule(schedule!!)
        }

        get("/{groupName}") {
            val userName = call.sessions.get<ExhibitSession>()!!.username
            val groupName = call.parameters["groupName"]!!
            val schedule = scheduleDAO.retrieveSchedule(userName, groupName)
            call.respond(schedule)
        }
    }
}