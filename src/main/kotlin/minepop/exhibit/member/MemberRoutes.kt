package minepop.exhibit.member

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import minepop.exhibit.auth.currentDate
import minepop.exhibit.auth.exhibitSession
import minepop.exhibit.auth.now
import minepop.exhibit.group.GroupDAO
import minepop.exhibit.schedule.IntervalSchedule
import minepop.exhibit.schedule.ScheduleDAO
import minepop.exhibit.schedule.WeeklySchedule
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

val scheduleDAO = ScheduleDAO()
val groupDAO = GroupDAO()

fun Route.memberRoutes() {
    route("member") {
        route("{groupId}") {
            get("/statistics") {

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
                            array.add(DayOfWeek.of(it).getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                        }
                        body.add("days", array)
                    } else if (schedule is IntervalSchedule) {
                        body.addProperty("days", schedule.days)
                    }

                    val now = exhibitSession().now()
                    val projectedSchedule = JsonArray()
                    schedule.iterate(iterateStart = now.minusDays(3), iterateEnd = now.plusDays(3)) {
                        projectedSchedule.add(it.toString())
                    }
                    body.add("projectedSchedule", projectedSchedule)
                    call.respond(body)
                }
            }
        }
    }
}