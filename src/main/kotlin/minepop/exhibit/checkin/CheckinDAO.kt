package minepop.exhibit.checkin

import minepop.exhibit.dao.DAO
import java.sql.Date
import java.sql.SQLException
import java.time.LocalDate
import java.time.ZoneId

class CheckinDAO : DAO() {

    fun createCheckin(userId: Long, groupId: Long, timeZone: String): Date {
        val date = Date.valueOf(LocalDate.now(ZoneId.of(timeZone)))
        connect().use { c ->
            c.prepareStatement("insert into checkin(user_id, group_id, date) values(?, ?, ?)").use {
                it.setLong(1, userId)
                it.setLong(2, groupId)
                it.setDate(3, date)
                it.executeUpdate()
            }
        }
        return date
    }

    fun retrieveCheckins(groupId: Long, timeZone: String, pastDays: Int, userName: String? = null): List<Checkin> {
        val checkins = mutableListOf<Checkin>()
        val date = Date.valueOf(LocalDate.now(ZoneId.of(timeZone)))
        connect().use { c ->
            var sql = "select user.name, date from checkin inner join user on user_id = user.id" +
                    " where date_add(date, interval ? day) > ? and group_id = ?"
            userName.let {
                sql += " and user.name = ?"
            }
            c.prepareStatement(sql).use {ps ->
                ps.setInt(1, pastDays)
                ps.setDate(2, date)
                ps.setLong(3, groupId)
                userName.let {
                    ps.setString(4, it)
                }
                val rs = ps.executeQuery()
                while (rs.next()) {
                    checkins.add(Checkin(rs.getString(1), rs.getDate(2)))
                }
            }
        }
        return checkins
    }
}