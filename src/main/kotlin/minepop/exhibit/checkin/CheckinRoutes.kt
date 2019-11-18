package minepop.exhibit.checkin

import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import minepop.exhibit.auth.exhibitSession

val checkinDAO = CheckinDAO()

fun Route.checkinRoutes() {
    route("checkin") {

        get("/{groupId}") {
            val pastDays = call.request.queryParameters["pastDays"]?.toIntOrNull()
            val groupId = call.parameters["groupId"]!!.toLong()
            if (pastDays != null) {
                val checkins = checkinDAO.retrieveCheckins(groupId, exhibitSession().timezone, pastDays)
                call.respond(checkins)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/{groupId}/@me") {
            val pastDays = call.request.queryParameters["pastDays"]?.toIntOrNull()
            val groupId = call.parameters["groupId"]!!.toLong()
            if (pastDays != null) {
                val checkins = checkinDAO.retrieveCheckins(groupId, exhibitSession().timezone, pastDays, exhibitSession().username)
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

        post("/{groupId}") {
            val groupId = call.parameters["groupId"]!!.toLong()
            val date = checkinDAO.createCheckin(exhibitSession().userid, groupId, exhibitSession().timezone)
            val body = JsonObject()
            body.addProperty("date", date.time)
            call.respond(body);
        }
    }
}