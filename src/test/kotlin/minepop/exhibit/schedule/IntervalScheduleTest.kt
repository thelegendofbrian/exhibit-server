package minepop.exhibit.schedule

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.sql.Date
import java.time.LocalDate

class IntervalScheduleTest {

    private var schedule: IntervalSchedule? = null
    private var stats: ScheduleStatsUpdate? = null

    @Before
    fun setUp() {
        schedule = IntervalSchedule(123, Date.valueOf(LocalDate.parse("2019-11-04")))
        schedule!!.days = 4
        stats = schedule.newStats()
    }

    @Test
    fun normal() {
        val now = LocalDate.parse("2019-11-12")
        val lastCheckin = LocalDate.parse("2019-11-08")

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now)

        assertFalse(stats!!.isBonusCheckin)
        assertEquals(0, stats!!.missedCheckins)
    }

    @Test
    fun bonus() {
        val now = LocalDate.parse("2019-11-11")
        val lastCheckin = LocalDate.parse("2019-11-08")

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now)

        assertTrue(stats!!.isBonusCheckin)
        assertEquals(0, stats!!.missedCheckins)
    }

    @Test
    fun normalMissed() {
        val now = LocalDate.parse("2019-11-20")
        val lastCheckin = LocalDate.parse("2019-11-08")

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now)

        assertFalse(stats!!.isBonusCheckin)
        assertEquals(2, stats!!.missedCheckins)
    }

    @Test
    fun bonusMissed() {
        val now = LocalDate.parse("2019-11-21")
        val lastCheckin = LocalDate.parse("2019-11-08")

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now)

        assertTrue(stats!!.isBonusCheckin)
        assertEquals(3, stats!!.missedCheckins)
    }
}