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
        get("/{groupName}") {
            val pastDays = call.request.queryParameters["pastDays"]?.toIntOrNull()
            val groupName = call.parameters["groupName"]!!
            if (pastDays != null) {
                val checkins = checkinDAO.retrieveCheckins(groupName, pastDays)
                call.respond(checkins)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
        post("/{groupName}") {
            val userName = call.sessions.get<ExhibitSession>()?.username
            val groupName = call.parameters["groupName"]!!
            val timeZoneOffset = call.request.queryParameters["timeZoneOffset"]?.toIntOrNull()
            if (userName != null && timeZoneOffset != null) {
                val date = checkinDAO.createCheckin(userName, groupName, timeZoneOffset)
                val body = JsonObject()
                body.addProperty("date", date.time)
                call.respond(body);
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}