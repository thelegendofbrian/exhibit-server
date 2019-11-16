package minepop.exhibit.checkin

import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import minepop.exhibit.exhibitSession

val checkinDAO = CheckinDAO()

fun Route.checkinRoutes() {
    route("checkin") {

        get("/{groupName}") {
            val pastDays = call.request.queryParameters["pastDays"]?.toIntOrNull()
            val groupName = call.parameters["groupName"]!!
            if (pastDays != null) {
                val checkins = checkinDAO.retrieveCheckins(groupName, exhibitSession().timezone, pastDays)
                call.respond(checkins)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/{groupName}/@me") {
            val pastDays = call.request.queryParameters["pastDays"]?.toIntOrNull()
            val groupName = call.parameters["groupName"]!!
            if (pastDays != null) {
                val checkins = checkinDAO.retrieveCheckins(exhibitSession().username, groupName, exhibitSession().timezone, pastDays)
                if (pastDays == 1) {
                    if (checkins.isEmpty()) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(checkins[0])
                    }
                } else {
                    call.respond(checkins)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("/{groupName}") {
            val groupName = call.parameters["groupName"]!!
            val date = checkinDAO.createCheckin(exhibitSession().username, groupName, exhibitSession().timezone)
            val body = JsonObject()
            body.addProperty("date", date.time)
            call.respond(body);
        }
    }
}