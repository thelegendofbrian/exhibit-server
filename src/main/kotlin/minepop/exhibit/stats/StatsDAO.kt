package minepop.exhibit.stats

import minepop.exhibit.checkin.Checkin
import minepop.exhibit.dao.DAO
import minepop.exhibit.schedule.ScheduleStatsUpdate
import java.sql.Date

class StatsDAO: DAO() {

    fun retrieveLastScheduledCheckin(groupMemberId: Long): Checkin? {
        connect().use { c ->
            c.prepareStatement("select date from checkin where group_member_id = ? and is_bonus = 'N' order by date desc limit 1").use {
                it.setLong(1, groupMemberId)
                val rs = it.executeQuery()
                if (rs.next()) {
                    return Checkin(rs.getDate(1), false)
                }
            }
        }
        return null
    }

    fun retrieveScheduledCheckins(groupMemberId: Long, date: Date, pastDays: Int): List<Checkin> {
        val checkins = mutableListOf<Checkin>()
        connect().use { c ->
            c.prepareStatement("select date from checkin where date_add(date, interval ? day) > ? and group_member_id = ? and is_bonus = 'N'").use {
                it.setInt(1, pastDays)
                it.setDate(2, date)
                it.setLong(3, groupMemberId)
                val rs = it.executeQuery()
                while (rs.next()) {
                    checkins.add(Checkin(rs.getDate(1), false))
                }
            }
        }
        return checkins
    }

    fun updateStats(stats: ScheduleStatsUpdate) {
        connect().use { c ->
            c.prepareStatement("update group_member_stats set" +
                    " streak = case when ? = 0 then case when ? then streak else streak + 1 end else 0 end," +
                    " regular_checkins = case when ? then regular_checkins + 1 else regular_checkins end," +
                    " bonus_checkins = case when ? then bonus_checkins + 1 else bonus_checkins end," +
                    " missed_checkins = missed_checkins + ?" +
                    " where group_member_id = ?").use {
                it.setInt(1, stats.missedCheckins)
                it.setBoolean(2, stats.isBonusCheckin)
                it.setBoolean(3, !stats.isBonusCheckin)
                it.setBoolean(4, stats.isBonusCheckin)
                it.setInt(5, stats.missedCheckins)
                it.setLong(6, stats.groupMemberId)
                it.executeUpdate()
            }
        }
    }

    fun retrieveStats(groupMemberId: Long): GroupMemberStatistics {
        connect().use { c ->
            c.prepareStatement("select streak, regular_checkins, bonus_checkins, missed_checkins, points" +
                    " from group_member_stats where group_member_id = ?").use {
                it.setLong(1, groupMemberId)
                val rs = it.executeQuery()
                rs.next()
                return GroupMemberStatistics(groupMemberId, rs.getInt(1),
                    rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getLong(5))
            }
        }
    }
}