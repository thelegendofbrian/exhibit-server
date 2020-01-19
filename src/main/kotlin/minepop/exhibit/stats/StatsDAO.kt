package minepop.exhibit.stats

import minepop.exhibit.checkin.Checkin
import minepop.exhibit.dao.DAO
import java.sql.Date

class StatsDAO: DAO() {

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

    fun updateStats(stats: GroupMemberStatistics) {
        connect().use { c ->
            c.prepareStatement("update group_member_stats set" +
                    " streak = ?," +
                    " regular_checkins = ?," +
                    " bonus_checkins = ?," +
                    " missed_checkins = ?" +
                    " where group_member_id = ?").use {
                it.setInt(1, stats.streak)
                it.setInt(2, stats.regularCheckins)
                it.setInt(3, stats.bonusCheckins)
                it.setInt(4, stats.missedCheckins)
                it.setLong(5, stats.groupMemberId)
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

    fun updateStatsState(groupMemberId: Long, lastUpdate: Date) {
        connect().use { c ->
            c.prepareStatement("update group_member_stats_state set last_update = ? where group_member_id = ?").use {
                it.setDate(1, lastUpdate)
                it.setLong(2, groupMemberId)
                it.executeUpdate()
            }
        }
    }

    fun transitionStatus(groupMemberId: Long, from: String, to: String): Boolean {
        connect().use { c ->
            c.prepareStatement("update group_member_stats_state set status_id = (select id from status where description = ?)" +
                    " where group_member_id = ? and status_id = (select id from status where description = ?)").use {
                it.setString(1, to)
                it.setLong(2, groupMemberId)
                it.setString(3, from)
                return it.executeUpdate() == 1
            }
        }
    }

    fun retrieveStatsState(groupMemberId: Long): StatsState? {
        connect().use { c ->
            c.prepareStatement("select status.description, last_update from group_member_stats_state" +
                    " inner join status on status_id = status.id where group_member_id = ?").use {
                it.setLong(1, groupMemberId)
                val rs = it.executeQuery()
                if (rs.next()) {
                    return StatsState(rs.getString(1), rs.getDate(2))
                }
            }
            return null
        }
    }
}