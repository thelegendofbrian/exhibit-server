package minepop.exhibit.checkin

import minepop.exhibit.dao.DAO
import java.sql.Date
import java.sql.SQLException
import java.time.LocalDate
import java.time.ZoneOffset

class CheckinDAO : DAO() {

    @Throws(SQLException::class)
    fun createCheckin(userName: String, timeZoneOffset: Int): Date {
        val date = Date.valueOf(LocalDate.now(ZoneOffset.of("-0${timeZoneOffset / 60}:00")))
        connect().use {
            c ->
            c.prepareStatement("insert into checkin(user_name, date) values(?, ?)").use {
                it.setString(1, userName)
                it.setDate(2, date)
                it.executeUpdate()
            }
        }
        return date
    }

    @Throws(SQLException::class)
    fun retrieveCheckins(pastDays: Int): List<Checkin> {
        val checkins = mutableListOf<Checkin>()
        connect().use {
                c ->
            c.prepareStatement("select user_name, date from checkin where date + ? days > current date").use {
                it.setInt(1, pastDays)
                val rs = it.executeQuery()
                while (rs.next()) {
                    checkins.add(Checkin(rs.getString(1), rs.getDate(2)))
                }
            }
        }
        return checkins
    }
}