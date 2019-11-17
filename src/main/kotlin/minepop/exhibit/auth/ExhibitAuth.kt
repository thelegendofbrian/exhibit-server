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
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import minepop.exhibit.Crypto
import minepop.exhibit.prod
import java.util.*

private val dao = SessionAuthDAO()

fun Authentication.Configuration.installExhibitAuth() {
    form(name = "Form") {
        userParamName = "username"
        passwordParamName = "password"
        skipWhen { it.sessions.get<ExhibitSession>() != null }
        validate { credentials ->

            request.cookies["Quick-Auth"]?.let {
                dao.retrieveUserForQuickAuth(it)?.let {
                    userName ->
                    sessions.set(ExhibitSession(userName, request.headers["timezone"]!!))
                    return@validate UserIdPrincipal(credentials.name)
                }
            }

            val user = dao.retrieveUser(credentials.name)
            if (user == null || user.failedLogins >= 5) {
                return@validate null
            }

            val digest = Crypto.hash(credentials.password.toCharArray(), user.salt)
            if (digest.contentEquals(user.saltedHash)) {
                sessions.set(ExhibitSession(credentials.name, request.headers["timezone"]!!))
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

fun Route.loginRoute() {
    post("login") {
        val userName = call.sessions.get<ExhibitSession>()?.username
        if (userName != null) {
            val body = JsonObject()
            val user = JsonObject()
            body.add("user", user)
            user.addProperty("name", userName)
            call.respond(body)
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}