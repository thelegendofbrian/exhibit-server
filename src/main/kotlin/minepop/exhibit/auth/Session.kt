package minepop.exhibit.auth

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.util.pipeline.PipelineContext

data class ExhibitSession(val username: String, val timezone: String)

fun PipelineContext<Unit, ApplicationCall>.exhibitSession(): ExhibitSession {
    return this.call.sessions.get<ExhibitSession>()!!
}