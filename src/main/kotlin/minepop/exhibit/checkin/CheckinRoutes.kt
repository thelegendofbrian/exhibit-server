package minepop.exhibit.checkin

import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import minepop.exhibit.ExhibitSession

val checkinDAO = CheckinDAO()

fun Route.checkinRoutes() {
    route("checkin") {
        get("/") {
            val pastDays = call.request.queryParameters["pastDays"]?.toIntOrNull()
            if (pastDays != null) {
                val checkins = checkinDAO.retrieveCheckins(pastDays)
                call.respond(checkins)
            }
            call.respond(HttpStatusCode.BadRequest)
        }
        post("/") {
            val userName = call.sessions.get<ExhibitSession>()?.username
            val timeZoneOffset = call.request.queryParameters["timeZoneOffset"]?.toIntOrNull()
            if (userName != null && timeZoneOffset != null) {
                val date = checkinDAO.createCheckin(userName, timeZoneOffset)
                val body = JsonObject()
                body.addProperty("date", date.time)
                call.respond(body);
            }
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}