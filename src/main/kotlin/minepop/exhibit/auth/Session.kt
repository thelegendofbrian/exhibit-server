package minepop.exhibit.auth

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.util.pipeline.PipelineContext
import minepop.exhibit.user.UserSettings

data class ExhibitSession(val userid: Long, val username: String, val timezone: String, val defaultGroupId: Long?)

fun PipelineContext<Unit, ApplicationCall>.exhibitSession(): ExhibitSession {
    return this.call.sessions.get<ExhibitSession>()!!
}