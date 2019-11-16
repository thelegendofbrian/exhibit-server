package minepop.exhibit.checkin

import minepop.exhibit.dao.DAO
import java.sql.Date
import java.sql.SQLException
import java.time.LocalDate
import java.time.ZoneId

class CheckinDAO : DAO() {

    @Throws(SQLException::class)
    fun createCheckin(userName: String, groupName: String, timeZone: String): Date {
        val date = Date.valueOf(LocalDate.now(ZoneId.of(timeZone)))
        connect().use {
            c ->
            c.prepareStatement("insert into checkin(user_name, group_name, date) values(?, ?, ?)").use {
                it.setString(1, userName)
                it.setString(2, groupName)
                it.setDate(3, date)
                it.executeUpdate()
            }
        }
        return date
    }

    @Throws(SQLException::class)
    fun retrieveCheckins(groupName: String, timeZone: String, pastDays: Int): List<Checkin> {
        val checkins = mutableListOf<Checkin>()
        val date = Date.valueOf(LocalDate.now(ZoneId.of(timeZone)))
        connect().use {
            c ->
            c.prepareStatement("select user_name, date from checkin where date_add(date,interval ? day) > ? and group_name = ?").use {
                it.setInt(1, pastDays)
                it.setDate(2, date)
                it.setString(3, groupName)
                val rs = it.executeQuery()
                while (rs.next()) {
                    checkins.add(Checkin(rs.getString(1), rs.getDate(2)))
                }
            }
        }
        return checkins
    }

    @Throws(SQLException::class)
    fun retrieveCheckins(userName: String, groupName: String, timeZone: String, pastDays: Int): List<Checkin> {
        val checkins = mutableListOf<Checkin>()
        val date = Date.valueOf(LocalDate.now(ZoneId.of(timeZone)))
        connect().use {
            c ->
            c.prepareStatement("select user_name, date from checkin where date_add(date,interval ? day) > ? and user_name = ? and group_name = ?").use {
                it.setInt(1, pastDays)
                it.setDate(2, date)
                it.setString(3, userName)
                it.setString(4, groupName)
                val rs = it.executeQuery()
                while (rs.next()) {
                    checkins.add(Checkin(rs.getString(1), rs.getDate(2)))
                }
            }
        }
        return checkins
    }
}