package minepop.exhibit.schedule

import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import java.sql.Date
import java.time.DayOfWeek
import java.time.LocalDate

class WeeklyScheduleTest {

    private var schedule: WeeklySchedule? = null
    private var stats: ScheduleStatsUpdate? = null

    @Before
    fun setUp() {
        schedule = WeeklySchedule(123, Date.valueOf(LocalDate.parse("2018-12-05")))
        schedule!!.days += DayOfWeek.MONDAY.value
        schedule!!.days += DayOfWeek.TUESDAY.value
        schedule!!.days += DayOfWeek.WEDNESDAY.value
        schedule!!.days += DayOfWeek.THURSDAY.value
        schedule!!.days += DayOfWeek.FRIDAY.value
        stats = schedule.newStats()
    }

    @Test
    fun calculateStats_PreviousDayCheckin() {
        val now = LocalDate.parse("2018-12-07") //Friday
        val lastCheckin = LocalDate.parse("2018-12-06") //Thursday

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, true)

        assertEquals(CheckinType.SCHEDULED, stats!!.checkinType)
        assertEquals(0, stats!!.missedCheckins)
    }

    @Test
    fun calculateStats_PreviousDayNoCheckin() {
        val now = LocalDate.parse("2018-12-07") //Friday
        val lastCheckin = LocalDate.parse("2018-12-06") //Thursday

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, false)

        assertEquals(CheckinType.NONE, stats!!.checkinType)
        assertEquals(0, stats!!.missedCheckins)
    }

    @Test
    fun calculateStats_TwoDaysAgoLastScheduledCheckin() {
        val now = LocalDate.parse("2018-12-10") //Monday
        val lastCheckin = LocalDate.parse("2018-12-07") //Friday

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, true)

        assertEquals(CheckinType.SCHEDULED, stats!!.checkinType)
        assertEquals(0, stats!!.missedCheckins)
    }

    @Test
    fun calculateStats_BonusCheckin() {
        val now = LocalDate.parse("2018-12-08") //Saturday
        val lastCheckin = LocalDate.parse("2018-12-07") //Friday

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, true)

        assertEquals(CheckinType.BONUS, stats!!.checkinType)
        assertEquals(0, stats!!.missedCheckins)
    }

    @Test
    fun calculateStats_BonusCheckinAndStreakBroken() { //Broken just like my heart
        val now = LocalDate.parse("2018-12-15") //Saturday
        val lastCheckin = LocalDate.parse("2018-12-07") //Friday

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, true)

        assertEquals(CheckinType.BONUS, stats!!.checkinType)
        assertEquals(5, stats!!.missedCheckins)
    }

    @Test
    fun calculateStats_StreakBrokenWithCheckin() {
        val now = LocalDate.parse("2018-12-11") //Tuesday
        val lastCheckin = LocalDate.parse("2018-12-07") //Friday

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, true)

        assertEquals(CheckinType.SCHEDULED, stats!!.checkinType)
        assertEquals(1, stats!!.missedCheckins)
    }

    @Test
    fun calculateStats_StreakBrokenNoCheckin() {
        val now = LocalDate.parse("2018-12-11") //Tuesday
        val lastCheckin = LocalDate.parse("2018-12-07") //Friday

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, false)

        assertEquals(CheckinType.NONE, stats!!.checkinType)
        assertEquals(1, stats!!.missedCheckins)
    }

    @Test
    fun calculateStats_StreakBrokenByTenDays() {
        val now = LocalDate.parse("2018-12-24") //Monday
        val lastCheckin = LocalDate.parse("2018-12-07") //Friday

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, true)

        assertEquals(CheckinType.SCHEDULED, stats!!.checkinType)
        assertEquals(10, stats!!.missedCheckins)
    }

    @Test
    fun calculateStats_StreakBrokenOnLeapYear() {
        val now = LocalDate.parse("2020-03-01") //Sunday
        val lastCheckin = LocalDate.parse("2020-02-27") //Thursday

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, true)

        assertEquals(CheckinType.BONUS, stats!!.checkinType)
        assertEquals(1, stats!!.missedCheckins)
    }
}