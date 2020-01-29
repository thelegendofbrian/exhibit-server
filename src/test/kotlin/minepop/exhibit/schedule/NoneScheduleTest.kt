package minepop.exhibit.schedule

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.sql.Date
import java.time.LocalDate

class NoneScheduleTest {

    private var schedule: NoneSchedule? = null
    private var stats: ScheduleStatsUpdate? = null

    @Before
    fun setUp() {
        schedule = NoneSchedule(-1, Date.valueOf(LocalDate.parse("2019-11-04")))
        stats = schedule.newStats()
    }

    @Test
    fun consecutiveCheckin() {
        val now = LocalDate.parse("2019-11-12")
        val lastCheckin = LocalDate.parse("2019-11-11")

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, true)

        assertEquals(CheckinType.SCHEDULED, stats!!.checkinType)
        assertEquals(0, stats!!.missedCheckins)
    }

    @Test
    fun missedCheckin_withCheckin() {
        val now = LocalDate.parse("2019-11-13")
        val lastCheckin = LocalDate.parse("2019-11-11")

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, true)

        assertEquals(CheckinType.SCHEDULED, stats!!.checkinType)
        assertEquals(1, stats!!.missedCheckins)
    }

    @Test
    fun noCheckin() {
        val now = LocalDate.parse("2019-11-12")
        val lastCheckin = LocalDate.parse("2019-11-11")

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, false)

        assertEquals(CheckinType.NONE, stats!!.checkinType)
        assertEquals(0, stats!!.missedCheckins)
    }

    @Test
    fun missedCheckin_noCheckin() {
        val now = LocalDate.parse("2019-11-13")
        val lastCheckin = LocalDate.parse("2019-11-11")

        schedule!!.calculateStatsUpdate(stats!!, lastCheckin, now, false)

        assertEquals(CheckinType.NONE, stats!!.checkinType)
        assertEquals(1, stats!!.missedCheckins)
    }
}