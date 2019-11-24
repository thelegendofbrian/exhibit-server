package minepop.exhibit.user

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import minepop.exhibit.Crypto
import minepop.exhibit.auth.AuthUser
import minepop.exhibit.auth.SessionAuthDAO

val settingsDAO = UserSettingsDAO()
val sessionAuthDAO = SessionAuthDAO()

fun Route.userRoutes() {
    route("/user/settings") {
        route("/settings") {
            post("/") {
                val settings = call.receive<UserSettings>()
                settingsDAO.updateSettings(settings)
                call.respond(HttpStatusCode.NoContent)
            }
        }
        route("/account") {
            post("/") {
                val accountPost = call.receive<AccountPost>()
                val authUser = sessionAuthDAO.retrieveUser(id = accountPost.userId)
                authUser?.salt = Crypto.nextSalt(32)
                authUser?.saltedHash = Crypto.hash(accountPost.newPassword.toCharArray(), authUser!!.salt)
                sessionAuthDAO.updateUser(authUser)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

data class AccountPost(val userId: Long, val newPassword: String)