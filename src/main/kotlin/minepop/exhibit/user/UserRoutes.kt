package minepop.exhibit.user

import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import minepop.exhibit.Crypto
import minepop.exhibit.auth.*

private val settingsDAO = UserSettingsDAO()
private val sessionAuthDAO = SessionAuthDAO()

fun Route.userRoutes() {

    route("user") {
        route("settings") {
            get("/") {
                val userId = exhibitSession().userId
                val settings = settingsDAO.retrieveSettings(userId)
                val response = JsonObject()
                response.addProperty("timeZone", settings.timeZone)
                response.addProperty("displayName", settings.displayName)
                response.addProperty("defaultGroupId", settings.defaultGroupId)
                response.addProperty("startOfWeek", settings.startOfWeek)
                call.respond(response)
            }
            post("/") {
                val request = call.receive<JsonObject>()
                val timeZone = request.get("timeZone").asString
                val defaultGroupIdElem = request.get("defaultGroupId")
                val defaultGroupId = if (defaultGroupIdElem.isJsonNull) null else defaultGroupIdElem.asLong
                val displayNameElem = request.get("displayName")
                val displayName = if (displayNameElem.isJsonNull) null else displayNameElem.asString
                val startOfWeek = request.get("startOfWeek").asInt
                settingsDAO.updateSettings(UserSettings(exhibitSession().userId, timeZone, defaultGroupId, displayName, startOfWeek))
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