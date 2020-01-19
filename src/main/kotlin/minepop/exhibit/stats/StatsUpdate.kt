package minepop.exhibit.stats

import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import minepop.exhibit.auth.exhibitSession
import minepop.exhibit.auth.now
import minepop.exhibit.group.GroupDAO
import minepop.exhibit.schedule.ScheduleDAO
import minepop.exhibit.schedule.ScheduleStatsUpdate
import minepop.exhibit.schedule.calculateStatsUpdate
import java.sql.Date
import kotlin.concurrent.thread

private val groupDAO = GroupDAO()
private val statsDAO = StatsDAO()
private val scheduleDAO = ScheduleDAO()

fun PipelineContext<Unit, ApplicationCall>.updateGroupStats(groupMemberId: Long, isCheckin: Boolean): ScheduleStatsUpdate? {

    val now = exhibitSession().now()
    val dateNow = Date.valueOf(now)
    val lastUpdate = statsDAO.retrieveStatsState(groupMemberId)!!.lastUpdate
    if (dateNow == lastUpdate && !isCheckin)
        return null

    if (!statsDAO.transitionStatus(groupMemberId, "Ready", "In Progress"))
        return null

    val schedules = scheduleDAO.retrieveSchedules(groupMemberId, lastUpdate, dateNow)
    val stats = schedules.calculateStatsUpdate(groupMemberId, lastUpdate?.toLocalDate(), now, isCheckin)
    val updatedStats = statsDAO.retrieveStats(groupMemberId).updateStatistics(stats)

    statsDAO.updateStats(updatedStats)
    statsDAO.transitionStatus(groupMemberId, "In Progress", "Ready")
    statsDAO.updateStatsState(groupMemberId, dateNow)

    return stats
}

fun PipelineContext<Unit, ApplicationCall>.startStatsThread() {
    thread(start = true) {

        val userId = exhibitSession().userId
        val groups = groupDAO.retrieveGroupsByMember(userId)

        groups.forEach {
            val groupMemberId = groupDAO.retrieveGroupMemberId(it.id, userId)!!
            updateGroupStats(groupMemberId, false)
        }
    }
}