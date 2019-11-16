package minepop.exhibit.checkin

import minepop.exhibit.dao.DAO
import java.sql.Date
import java.sql.SQLException
import java.time.LocalDate
import java.time.ZoneOffset

class CheckinDAO : DAO() {

    @Throws(SQLException::class)
    fun createCheckin(userName: String, groupName: String, timeZoneOffset: Int): Date {
        val date = Date.valueOf(LocalDate.now(ZoneOffset.of("-0${timeZoneOffset / 60}:00")))
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
    fun retrieveCheckins(groupName: String, pastDays: Int): List<Checkin> {
        val checkins = mutableListOf<Checkin>()
        connect().use {
            c ->
            // FIXME: Add time zone as parameter
            c.prepareStatement("select user_name, date from checkin where date_add(date,interval ? day) > CONVERT_TZ(now(),'+00:00','-08:00') and group_name = ?").use {
                it.setInt(1, pastDays)
                it.setString(2, groupName)
                //it.setString(3, userTimeZone)
                val rs = it.executeQuery()
                while (rs.next()) {
                    checkins.add(Checkin(rs.getString(1), rs.getDate(2)))
                }
            }
        }
        return checkins
    }
}