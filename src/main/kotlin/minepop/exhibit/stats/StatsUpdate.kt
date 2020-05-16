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
    val oldStats = statsDAO.retrieveStats(groupMemberId)
    val updatedStats = oldStats.updateStatistics(stats)

    statsDAO.updateStats(updatedStats)
    /*
        Only advance lastUpdate to today if a checkin was made.
        Otherwise, advance to yesterday.
     */
    statsDAO.updateStatsState(groupMemberId, if (isCheckin) dateNow else Date.valueOf(now.minusDays(1)))
    statsDAO.transitionStatus(groupMemberId, "In Progress", "Ready")

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