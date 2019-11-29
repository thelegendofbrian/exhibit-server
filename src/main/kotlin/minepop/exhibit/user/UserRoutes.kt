package minepop.exhibit.user

import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import minepop.exhibit.Crypto
import minepop.exhibit.auth.*

private val settingsDAO = UserSettingsDAO()
private val sessionAuthDAO = SessionAuthDAO()

fun Route.userRoutes() {

    route("user") {
        route("settings") {
            post("/") {
                val request = call.receive<JsonObject>()
                val timeZone = request.get("timeZone").asString
                val defaultGroupId = request.get("defaultGroupId").asLong
                val displayName = request.get("displayName").asString
                settingsDAO.updateSettings(UserSettings(exhibitSession().userId, timeZone, defaultGroupId, displayName))
                call.respond(HttpStatusCode.NoContent)
            }
        }
        route("account") {
            post("/") {
                val request = call.receive<JsonObject>()
                val newPassword = request.get("newPassword").asString
                val authUser = sessionAuthDAO.retrieveUser(id = exhibitSession().userId)!!
                authUser.salt = Crypto.nextSalt(32)
                authUser.saltedHash = Crypto.hash(newPassword.toCharArray(), authUser.salt)
                sessionAuthDAO.updateUser(authUser)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}