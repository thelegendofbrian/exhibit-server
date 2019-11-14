package minepop.exhibit.schedule

import minepop.exhibit.dao.DAO
import java.sql.PreparedStatement
import java.sql.SQLException

class ScheduleDAO: DAO() {

    @Throws(SQLException::class)
    fun createUpdateSchedule(schedule: Schedule) {
        connect().use {
            c ->

            c.prepareStatement("delete from schedule where user_name = ? and group_name = ?").use {
                it.setString(1, schedule.userName)
                it.setString(2, schedule.groupName)
                it.executeUpdate()
            }

            var scheduleTypeId: Int? = null
            c.prepareStatement("select id from schedule_type where name = ?").use {
                it.setString(1, if (schedule is WeeklySchedule) "Weekly" else "Interval")
                val rs = it.executeQuery()
                if (rs.next()) {
                    scheduleTypeId = rs.getInt(1)
                }
            }

            var scheduleId: Int? = null
            c.prepareStatement("insert into schedule(user_name, group_name, schedule_type_id) values(?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS).use {
                it.setString(1, schedule.userName)
                it.setString(2, schedule.groupName)
                it.setInt(3, scheduleTypeId!!)
                it.executeUpdate()
                scheduleId =  it.generatedKeys.getInt(1)
            }

            when (schedule) {
                is WeeklySchedule -> schedule.days!!.forEach {
                    day ->
                    c.prepareStatement("insert into schedule_weekly(id, day_of_week_id) values(?, (select id from day_of_week where day = ?))").use {
                        it.setInt(1, scheduleId!!)
                        it.setString(2, day)
                        it.executeUpdate()
                    }
                }
                is IntervalSchedule -> c.prepareStatement("insert into schedule_interval(id, interval_days) values(?, ?)").use {
                    it.setInt(1, scheduleId!!)
                    it.setInt(2, schedule.days!!)
                    it.executeUpdate()
                }
            }

            return@use
        }
    }

    @Throws(SQLException::class)
    fun retrieveSchedule(userName: String, groupName: String): Schedule {
        var schedule: Schedule? = null
        connect().use {
            c ->

            var scheduleId: Int? = null

            c.prepareStatement("select id from schedule where user_name = ? and group_name = ?").use {
                it.setString(1, userName)
                it.setString(2, groupName)
                val rs = it.executeQuery()
                if (rs.next()) {
                    scheduleId = rs.getInt(1)
                }
            }

            c.prepareStatement("select name from schedule_type where id = ?").use {
                it.setInt(1, scheduleId!!)
                val rs = it.executeQuery()
                if (rs.next()) {
                    schedule = if (rs.getString(1) == "Weekly") WeeklySchedule(userName, groupName) else IntervalSchedule(userName, groupName)
                }
            }

            when (schedule) {
                is WeeklySchedule -> {
                    c.prepareStatement("select name from schedule_weekly sched inner join day_of_week day on sched.day_of_week_id = day.id where id = ?").use {
                        it.setInt(1, scheduleId!!)
                        val rs = it.executeQuery()
                        while (rs.next()) {
                            (schedule as WeeklySchedule).days += rs.getString(1)
                        }
                    }
                }
                is IntervalSchedule -> {
                    c.prepareStatement("select interval_days from schedule_interval where schedule_id = ?").use {
                        it.setInt(1, scheduleId!!)
                        val rs = it.executeQuery()
                        if (rs.next()) {
                            (schedule as IntervalSchedule).days = rs.getInt(1)
                        }
                    }
                }
            }
        }
        return schedule!!
    }
}