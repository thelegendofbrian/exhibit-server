package minepop.exhibit.auth

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.util.pipeline.PipelineContext
import minepop.exhibit.user.UserSettings
import java.sql.Date
import java.time.LocalDate
import java.time.ZoneId

data class ExhibitSession(val userId: Long, val userName: String, val timezone: String, val defaultGroupId: Long?)

fun PipelineContext<Unit, ApplicationCall>.exhibitSession(): ExhibitSession {
    return this.call.sessions.get<ExhibitSession>()!!
}

fun ExhibitSession.currentDate(): Date {
    return Date.valueOf(now())
}

fun ExhibitSession.now(): LocalDate {
    return LocalDate.now(ZoneId.of(timezone))
}