package minepop.exhibit.schedule

import org.junit.Test
import org.junit.Assert.*
import java.sql.Date
import java.time.DayOfWeek
import java.time.LocalDate

class ScheduleListTest {

    @Test
    fun noSchedule() {
        val schedules: List<WeeklySchedule> = mutableListOf()

        val now = LocalDate.parse("2018-12-07") //Friday
        val lastCheckin = LocalDate.parse("2018-12-06") //Thursday

        val stats = schedules.calculateStatsUpdate(123, lastCheckin, now)

        assertTrue(stats.isBonusCheckin)
        assertEquals(0, stats.missedCheckins)
    }

    @Test
    fun oneSchedule() {
        val schedules: MutableList<WeeklySchedule> = mutableListOf()
        val schedule = WeeklySchedule(123, Date.valueOf(LocalDate.parse("2018-12-05")))
        schedule.days += DayOfWeek.MONDAY.value
        schedule.days += DayOfWeek.TUESDAY.value
        schedule.days += DayOfWeek.WEDNESDAY.value
        schedule.days += DayOfWeek.THURSDAY.value
        schedule.days += DayOfWeek.FRIDAY.value
        schedules += schedule

        val now = LocalDate.parse("2018-12-07") //Friday
        val lastCheckin = LocalDate.parse("2018-12-06") //Thursday

        val stats = schedules.calculateStatsUpdate(123, lastCheckin, now)

        assertFalse(stats.isBonusCheckin)
        assertEquals(0, stats.missedCheckins)
    }

    @Test
    fun oneSchedule_missedCheckin() {
        val schedules: MutableList<WeeklySchedule> = mutableListOf()
        val schedule = WeeklySchedule(123, Date.valueOf(LocalDate.parse("2018-12-05")))
        schedule.days += DayOfWeek.MONDAY.value
        schedule.days += DayOfWeek.TUESDAY.value
        schedule.days += DayOfWeek.WEDNESDAY.value
        schedule.days += DayOfWeek.THURSDAY.value
        schedule.days += DayOfWeek.FRIDAY.value
        schedules += schedule

        val now = LocalDate.parse("2018-12-11") //Tuesday
        val lastCheckin = LocalDate.parse("2018-12-06") //Thursday

        val stats = schedules.calculateStatsUpdate(123, lastCheckin, now)

        assertFalse(stats.isBonusCheckin)
        assertEquals(2, stats.missedCheckins)
    }

    @Test
    fun threeSchedule() {
        val schedules: MutableList<WeeklySchedule> = mutableListOf()
        val schedule1 = WeeklySchedule(123, Date.valueOf(LocalDate.parse("2018-11-05")))
        // Expecting 2 missed checkins for this shedule
        schedule1.days += DayOfWeek.MONDAY.value
        schedules += schedule1
        val schedule2 = WeeklySchedule(123, Date.valueOf(LocalDate.parse("2018-11-15")))
        // Expecting 1 missed checkin for this schedule
        schedule2.days += DayOfWeek.WEDNESDAY.value
        schedules += schedule2
        val schedule3 = WeeklySchedule(123, Date.valueOf(LocalDate.parse("2018-11-25")))
        // Expecting 2 missed checkins for this schedule
        schedule3.days += DayOfWeek.SUNDAY.value
        schedules += schedule3

        val now = LocalDate.parse("2018-12-09") //?
        val lastCheckin = LocalDate.parse("2018-10-25") //?

        val stats = schedules.calculateStatsUpdate(123, lastCheckin, now)

        assertFalse(stats.isBonusCheckin)
        assertEquals(5, stats.missedCheckins)
    }
}