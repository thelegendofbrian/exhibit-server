package minepop.exhibit.schedule

import minepop.exhibit.dao.DAO
import java.sql.Date
import java.sql.PreparedStatement

class ScheduleDAO: DAO() {

    fun createUpdateSchedule(schedule: Schedule) {
        connect().use { c ->

            c.prepareStatement("delete from schedule where group_member_id = ?").use {
                it.setLong(1, schedule.groupMemberId)
                it.executeUpdate()
            }

            var scheduleTypeId: Int? = null
            c.prepareStatement("select id from schedule_type where name = ?").use {
                it.setString(1, if (schedule is WeeklySchedule) "Weekly" else if (schedule is IntervalSchedule) "Interval" else "None")
                val rs = it.executeQuery()
                if (rs.next()) {
                    scheduleTypeId = rs.getInt(1)
                }
            }

            var scheduleId: Int? = null
            c.prepareStatement("insert into schedule(group_member_id, schedule_type_id, start_date) values(?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS).use {

                it.setLong(1, schedule.groupMemberId)
                it.setInt(2, scheduleTypeId!!)
                it.setDate(3, schedule.startDate)
                it.executeUpdate()
                val keys = it.generatedKeys
                keys.next()
                scheduleId =  keys.getInt(1)
            }

            when (schedule) {
                is WeeklySchedule -> schedule.days.forEach { day ->
                    c.prepareStatement("insert into schedule_weekly(schedule_id, day_of_week_id) values(?, ?)").use {
                        it.setInt(1, scheduleId!!)
                        it.setInt(2, day)
                        it.executeUpdate()
                    }
                }
                is IntervalSchedule -> c.prepareStatement("insert into schedule_interval(schedule_id, interval_days) values(?, ?)").use {
                    it.setInt(1, scheduleId!!)
                    it.setInt(2, schedule.days!!)
                    it.executeUpdate()
                }
            }

            return@use
        }
    }

    fun retrieveSchedule(groupMemberId: Long, date: Date): Schedule? {
        connect().use { c ->

            var scheduleId: Long? = null
            var startDate: Date? = null
            var scheduleTypeId: Int? = null

            c.prepareStatement("select id, start_date, schedule_type_id from schedule where group_member_id = ? and start_date <= ? order by start_date desc limit 1").use {
                it.setLong(1, groupMemberId)
                it.setDate(2, date)
                val rs = it.executeQuery()
                if (rs.next()) {
                    scheduleId = rs.getLong(1)
                    startDate = rs.getDate(2)
                    scheduleTypeId = rs.getInt(3)
                } else {
                    return null
                }
            }

            return retrieveSchedule(scheduleId!!, scheduleTypeId!!, groupMemberId, startDate!!)
        }
    }

    fun retrieveSchedules(groupMemberId: Long, start: Date?, end: Date): List<Schedule> {
        val schedules = mutableListOf<Schedule>()
        connect().use { c ->

            val scheduleIds = mutableListOf<Long>()
            val scheduleTypeIds = mutableListOf<Int>()
            val startDates = mutableListOf<Date>()

            var sql = "select id, start_date, schedule_type_id from schedule where group_member_id = ? and start_date <= ?"
            start?.let {
                sql += " and start_date > ?"
            }
            sql += " order by start_date asc"

            c.prepareStatement(sql).use { ps ->
                ps.setLong(1, groupMemberId)
                ps.setDate(2, end)
                start?.let {
                    ps.setDate(3, start)
                }
                val rs = ps.executeQuery()
                while (rs.next()) {
                    scheduleIds += rs.getLong(1)
                    startDates += rs.getDate(2)
                    scheduleTypeIds += rs.getInt(3)
                }
            }

            for (i in scheduleIds.indices) {
                val scheduleId = scheduleIds[i]
                val startDate = startDates[i]
                val scheduleTypeId = scheduleTypeIds[i]
                schedules += retrieveSchedule(scheduleId, scheduleTypeId, groupMemberId, startDate)!!
            }
        }
        start?.let { it ->
            retrieveSchedule(groupMemberId, it)?.let {
                schedules += it
            }
        }
        return schedules
    }

    private fun retrieveSchedule(scheduleId: Long, scheduleTypeId: Int, groupMemberId: Long, startDate: Date): Schedule? {
        var schedule: Schedule? = null
        connect().use { c ->

            c.prepareStatement("select name from schedule_type where id = ?").use {
                it.setInt(1, scheduleTypeId)
                val rs = it.executeQuery()
                if (rs.next()) {
                    val type = rs.getString(1)
                    schedule = when (type) {
                        "Weekly" -> WeeklySchedule(groupMemberId, startDate)
                        "Interval" -> IntervalSchedule(groupMemberId, startDate)
                        else -> NoneSchedule(groupMemberId, startDate)
                    }
                }
            }

            when (schedule) {
                is WeeklySchedule -> {
                    c.prepareStatement("select day_of_week_id from schedule_weekly where schedule_id = ?").use {
                        it.setLong(1, scheduleId)
                        val rs = it.executeQuery()
                        while (rs.next()) {
                            (schedule as WeeklySchedule).days += rs.getInt(1)
                        }
                    }
                }
                is IntervalSchedule -> {
                    c.prepareStatement("select interval_days from schedule_interval where schedule_id = ?").use {
                        it.setLong(1, scheduleId)
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