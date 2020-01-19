package minepop.exhibit.stats

import minepop.exhibit.schedule.CheckinType
import minepop.exhibit.schedule.ScheduleStatsUpdate
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupMemberStatisticsTest {

    private val stats = GroupMemberStatistics(-1, 100, 0, 0, 0, 0)

    @Test
    fun updateStatistics_scheduled() {
        val newStats = stats.updateStatistics(ScheduleStatsUpdate(-1, CheckinType.SCHEDULED, 0))
        assertEquals(101, newStats.streak)
        assertEquals(1, newStats.regularCheckins)
        assertEquals(0, newStats.bonusCheckins)
        assertEquals(0, newStats.missedCheckins)
    }

    @Test
    fun updateStatistics_scheduledMissed() {
        val newStats = stats.updateStatistics(ScheduleStatsUpdate(-1, CheckinType.SCHEDULED, 15))
        assertEquals(1, newStats.streak)
        assertEquals(1, newStats.regularCheckins)
        assertEquals(0, newStats.bonusCheckins)
        assertEquals(15, newStats.missedCheckins)
    }

    @Test
    fun updateStatistics_bonus() {
        val newStats = stats.updateStatistics(ScheduleStatsUpdate(-1, CheckinType.BONUS, 0))
        assertEquals(100, newStats.streak)
        assertEquals(0, newStats.regularCheckins)
        assertEquals(1, newStats.bonusCheckins)
        assertEquals(0, newStats.missedCheckins)
    }

    @Test
    fun updateStatistics_bonusMissed() {
        val newStats = stats.updateStatistics(ScheduleStatsUpdate(-1, CheckinType.BONUS, 15))
        assertEquals(0, newStats.streak)
        assertEquals(0, newStats.regularCheckins)
        assertEquals(1, newStats.bonusCheckins)
        assertEquals(15, newStats.missedCheckins)
    }

    @Test
    fun updateStatistics_none() {
        val newStats = stats.updateStatistics(ScheduleStatsUpdate(-1, CheckinType.NONE, 0))
        assertEquals(100, newStats.streak)
        assertEquals(0, newStats.regularCheckins)
        assertEquals(0, newStats.bonusCheckins)
        assertEquals(0, newStats.missedCheckins)
    }

    @Test
    fun updateStatistics_noneMissed() {
        val newStats = stats.updateStatistics(ScheduleStatsUpdate(-1, CheckinType.NONE, 15))
        assertEquals(0, newStats.streak)
        assertEquals(0, newStats.regularCheckins)
        assertEquals(0, newStats.bonusCheckins)
        assertEquals(15, newStats.missedCheckins)
    }
}