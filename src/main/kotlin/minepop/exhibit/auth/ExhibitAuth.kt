package minepop.exhibit.auth

import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.form
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.sessions.clear
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import minepop.exhibit.Crypto
import minepop.exhibit.prod
import minepop.exhibit.user.UserSettingsDAO
import java.util.*

private val dao = SessionAuthDAO()
private val userSettingsDAO = UserSettingsDAO()

fun Authentication.Configuration.installExhibitAuth() {
    form(name = "Form") {
        userParamName = "username"
        passwordParamName = "password"
        skipWhen { it.sessions.get<ExhibitSession>() != null }
        validate { credentials ->

            val timezone = request.headers["timezone"]!!
            request.cookies["Quick-Auth"]?.let {
                dao.retrieveUserForQuickAuth(it)?.let { user ->
                    sessions.set(user.newSession())
                    return@validate UserIdPrincipal(credentials.name)
                }
            }

            val user = dao.retrieveUser(credentials.name)
            if (user == null || user.failedLogins >= 5) {
                return@validate null
            }

            if (user.userSettings.timezone == null) {
                user.userSettings.timezone = timezone
                userSettingsDAO.updateSettings(user.userSettings)
            }

            val digest = Crypto.hash(credentials.password.toCharArray(), user.salt)
            if (digest.contentEquals(user.saltedHash)) {
                sessions.set(user.newSession())
                val quickAuthKey = UUID.randomUUID().toString()

                response.cookies.append("Quick-Auth", quickAuthKey, maxAge = 2000000000, secure = prod, httpOnly = true)

                dao.createQuickAuth(user, quickAuthKey)
                return@validate UserIdPrincipal(credentials.name)
            }
            else {
                return@validate null
            }
        }
    }
}

fun Route.authRoutes() {
    post("login") {
        val body = JsonObject()
        body.addProperty("userName", exhibitSession().userName)
        body.addProperty("userId", exhibitSession().userId)
        call.respond(body)
    }
    post("logout") {
        call.sessions.clear<ExhibitSession>()
        call.response.cookies.appendExpired("Quick-Auth")
        call.respond(HttpStatusCode.NoContent)
    }
}