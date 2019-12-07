package minepop.exhibit.member

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import minepop.exhibit.auth.exhibitSession
import minepop.exhibit.auth.now
import minepop.exhibit.group.GroupDAO
import minepop.exhibit.schedule.ScheduleDAO
import minepop.exhibit.schedule.iterate
import minepop.exhibit.stats.GroupMemberCalculatedStatistics
import minepop.exhibit.stats.StatsDAO
import minepop.exhibit.stats.calculateStatistics
import java.sql.Date

private val groupDAO = GroupDAO()
private val statsDAO = StatsDAO()
private val scheduleDAO = ScheduleDAO()

fun Route.memberStatisticsRoutes() {
    get("/") {
        val groupId = call.parameters["groupId"]!!.toLong()
        val pastDays = call.request.queryParameters["pastDays"]?.toInt()
        val userId = exhibitSession().userId
        val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!
        val stats = statsDAO.retrieveStats(groupMemberId)
        val calcStats = stats.calculateStatistics()

        pastDays?.let {
            val now = exhibitSession().now()
            val dateNow = Date.valueOf(now)
            val start = now.minusDays(pastDays.toLong())
            val schedules = scheduleDAO.retrieveSchedules(groupMemberId, Date.valueOf(start), dateNow)
            var possibleCheckins = 0
            schedules.iterate(start, now) {
                possibleCheckins++
            }
            val scheduledCheckins = statsDAO.retrieveScheduledCheckins(groupMemberId, dateNow, pastDays).size
            val adherence = if (possibleCheckins == 0) null else scheduledCheckins.toDouble() / possibleCheckins

            calcStats.adherence = adherence
        }

        call.respond(calcStats)
    }
}