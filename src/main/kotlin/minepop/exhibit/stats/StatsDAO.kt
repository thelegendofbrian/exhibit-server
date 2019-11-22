package minepop.exhibit.stats

import minepop.exhibit.checkin.Checkin
import minepop.exhibit.dao.DAO

class StatsDAO: DAO() {

    fun retrieveLastScheduledCheckin(groupMemberId: Long): Checkin? {
        connect().use { c ->
            c.prepareStatement("select date, is_bonus from checkin where group_member_id = ? order by date desc limit 1").use {
                it.setLong(1, groupMemberId)
                val rs = it.executeQuery()
                if (rs.next()) {
                    return Checkin(groupMemberId, rs.getDate(1), rs.getString(2) == "Y")
                }
            }
        }
        return null
    }

    fun updateStats(groupMemberId: Long, isStreakBroken: Boolean, isBonusCheckin: Boolean, missedCheckins: Int) {
        connect().use { c ->
            c.prepareStatement("update group_member_stats set" +
                    " streak = case when ? then streak + 1 else 0 end," +
                    " regular_checkins = case when ? then regular_checkins + 1 else regular_checkins," +
                    " bonus_checkins = case when ? then bonus_checkins + 1 else bonus_checkins," +
                    " missed_checkins = missed_checkins + ?" +
                    " where group_member_id = ?").use {
                it.setBoolean(1, !isStreakBroken)
                it.setBoolean(2, !isBonusCheckin)
                it.setBoolean(3, isBonusCheckin)
                it.setInt(4, missedCheckins)
                it.setLong(5, groupMemberId)
                it.executeUpdate()
            }
        }
    }
}