package minepop.exhibit.checkin

import minepop.exhibit.dao.DAO
import java.sql.Date

class CheckinDAO : DAO() {

    fun createCheckin(groupdMemberId: Long, date: Date, isBonus: Boolean) {
        connect().use { c ->
            c.prepareStatement("insert into checkin(group_member_id, date, is_bonus) values(?, ?, ?)").use {
                it.setLong(1, groupdMemberId)
                it.setDate(2, date)
                it.setString(3, if (isBonus) "Y" else "N")
                it.executeUpdate()
            }
        }
    }

    fun retrieveGroupMemberCheckins(groupMemberId: Long, date: Date, pastDays: Int): List<Checkin> {
        val checkins = mutableListOf<Checkin>()
        connect().use { c ->
            c.prepareStatement("select date, is_bonus from checkin where date_add(date, interval ? day) > ? and group_member_id = ?").use {
                it.setInt(1, pastDays)
                it.setDate(2, date)
                it.setLong(3, groupMemberId)
                val rs = it.executeQuery()
                while (rs.next()) {
                    checkins.add(Checkin(rs.getDate(1), rs.getString(2) == "Y"))
                }
            }
        }
        return checkins
    }

    fun retrieveGroupMemberCheckins(groupMemberId: Long, start: Date, end: Date): List<Checkin> {
        val checkins = mutableListOf<Checkin>()
        connect().use { c ->
            c.prepareStatement("select date, is_bonus from checkin where date > ? and date < ? and group_member_id = ? order by date asc").use {
                    ps ->
                ps.setDate(1, start)
                ps.setDate(2, end)
                ps.setLong(3, groupMemberId)
                val rs = ps.executeQuery()
                while (rs.next()) {
                    checkins.add(Checkin(rs.getDate(1), rs.getString(2) == "Y"))
                }
            }
        }
        return checkins
    }

    fun retrieveGroupCheckins(groupId: Long, date: Date, pastDays: Int): List<Checkin> {
        val checkins = mutableListOf<Checkin>()
        connect().use { c ->
            c.prepareStatement("select date, is_bonus from checkin where date_add(date, interval ? day) > ? and group_id = ?").use {
                    ps ->
                ps.setInt(1, pastDays)
                ps.setDate(2, date)
                ps.setLong(3, groupId)
                val rs = ps.executeQuery()
                while (rs.next()) {
                    checkins.add(Checkin(rs.getDate(1), rs.getString(2) == "Y"))
                }
            }
        }
        return checkins
    }
}