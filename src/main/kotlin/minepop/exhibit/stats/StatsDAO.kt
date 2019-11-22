package minepop.exhibit.stats

import minepop.exhibit.checkin.Checkin
import minepop.exhibit.dao.DAO
import minepop.exhibit.schedule.ScheduleStats

class StatsDAO: DAO() {

    fun retrieveLastScheduledCheckin(groupMemberId: Long): Checkin? {
        connect().use { c ->
            c.prepareStatement("select date from checkin where group_member_id = ? and is_bonus = 'N' order by date desc limit 1").use {
                it.setLong(1, groupMemberId)
                val rs = it.executeQuery()
                if (rs.next()) {
                    return Checkin(groupMemberId, rs.getDate(1), false)
                }
            }
        }
        return null
    }

    fun updateStats(stats: ScheduleStats) {
        connect().use { c ->
            c.prepareStatement("update group_member_stats set" +
                    " streak = case when ? then streak + 1 else 0 end," +
                    " regular_checkins = case when ? then regular_checkins + 1 else regular_checkins end," +
                    " bonus_checkins = case when ? then bonus_checkins + 1 else bonus_checkins end," +
                    " missed_checkins = missed_checkins + ?" +
                    " where group_member_id = ?").use {
                it.setBoolean(1, !stats.isStreakBroken)
                it.setBoolean(2, !stats.isBonusCheckin)
                it.setBoolean(3, stats.isBonusCheckin)
                it.setInt(4, stats.missedCheckins)
                it.setLong(5, stats.groupMemberId)
                it.executeUpdate()
            }
        }
    }
}