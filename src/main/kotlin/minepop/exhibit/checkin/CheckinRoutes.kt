package minepop.exhibit.checkin

import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import minepop.exhibit.auth.currentDate
import minepop.exhibit.auth.exhibitSession
import minepop.exhibit.auth.now
import minepop.exhibit.group.GroupDAO
import minepop.exhibit.schedule.IntervalSchedule
import minepop.exhibit.schedule.ScheduleDAO
import minepop.exhibit.schedule.WeeklySchedule
import minepop.exhibit.stats.StatsDAO
import java.sql.Date

val checkinDAO = CheckinDAO()
val scheduleDAO = ScheduleDAO()
val groupDAO = GroupDAO()
val statsDAO = StatsDAO()

fun Route.checkinRoutes() {
    route("checkin") {

        get("/{groupId}") {
            val pastDays = call.request.queryParameters["pastDays"]?.toIntOrNull()
            val groupId = call.parameters["groupId"]!!.toLong()
            if (pastDays != null) {
                val checkins = checkinDAO.retrieveCheckins(groupId, exhibitSession().currentDate(), pastDays)
                call.respond(checkins)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/{groupId}/@me") {
            val pastDays = call.request.queryParameters["pastDays"]?.toIntOrNull()
            val groupId = call.parameters["groupId"]!!.toLong()
            if (pastDays != null) {
                val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, exhibitSession().userid)!!
                val checkins = checkinDAO.retrieveCheckins(groupMemberId, exhibitSession().currentDate(), pastDays)
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

        post("/{groupId}") {
            val groupId = call.parameters["groupId"]!!.toLong()
            val userId = exhibitSession().userid
            val now = exhibitSession().now()
            val date = Date.valueOf(now)

            val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!
            val schedule = scheduleDAO.retrieveSchedule(groupMemberId, date)
            val lastCheckin = statsDAO.retrieveLastScheduledCheckin(groupMemberId)!!.date.toLocalDate()

            // determine if streak is broken
            var isStreakBroken = false // p sure this value never used
            var isBonus = false // p sure this value never used
            var missedCheckins = 0
            when (schedule) {
                is WeeklySchedule -> {
                    // go backwards through scheduled weekdays from today, until day matches last checkin
                    for (i in 1..7) {
                        val weekDay = now.dayOfWeek.value - i
                        if (schedule.days.contains(weekDay)) {
                            isStreakBroken = now.minusDays(i.toLong()) != lastCheckin
                            break
                        }
                    }
                    isBonus = schedule.days.contains(now.dayOfWeek.value)

                    var weekDay = lastCheckin
                    while (weekDay < now) {
                        weekDay = weekDay.plusDays(1)
                        if (schedule.days.contains(weekDay.dayOfWeek.value)) {
                            missedCheckins++
                        }
                    }
                }
                is IntervalSchedule -> {
                    // this could perform poorly
                    var intervalDay = schedule.startDate.toLocalDate()
                    val interval = schedule.days!!.toLong()
                    while (intervalDay <= lastCheckin) {
                        intervalDay = intervalDay.plusDays(interval)
                    }
                    isStreakBroken = lastCheckin != intervalDay
                    while (intervalDay <= now) {
                        intervalDay = intervalDay.plusDays(interval)
                        // 1 iteration forward means it is the very next scheduled checkin
                        missedCheckins++
                    }
                    isBonus = now != intervalDay
                    missedCheckins--
                }
            }

            statsDAO.updateStats(groupMemberId, isStreakBroken, isBonus, missedCheckins)
            checkinDAO.createCheckin(groupMemberId, date, isBonus)
            val body = JsonObject()
            body.addProperty("date", date.time)
            call.respond(body)
        }
    }
}