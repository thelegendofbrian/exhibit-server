package minepop.exhibit.auth

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
import minepop.exhibit.group.Group
import minepop.exhibit.group.GroupDAO
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
                    user ->
                    sessions.set(ExhibitSession(user.id, user.name, request.headers["timezone"]!!))
                    return@validate UserIdPrincipal(credentials.name)
                }
            }

            val user = dao.retrieveUser(credentials.name)
            if (user == null || user.failedLogins >= 5) {
                return@validate null
            }

            val digest = Crypto.hash(credentials.password.toCharArray(), user.salt)
            if (digest.contentEquals(user.saltedHash)) {
                sessions.set(ExhibitSession(user.id, user.name, request.headers["timezone"]!!))
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

val groupDAO = GroupDAO()

data class LoginResponse(val user: LoginResponseUser)
data class LoginResponseUser(val name: String, val groups: List<Group>)

fun Route.authRoutes() {
    post("login") {
        val userName = exhibitSession().username
        val groups = groupDAO.retrieveGroups(userName);
        val user = LoginResponseUser(userName, groups)
        call.respond(LoginResponse(user))
    }
    post("logout") {
        call.sessions.clear<ExhibitSession>()
        call.response.cookies.appendExpired("Quick-Auth")
        call.respond(HttpStatusCode.NoContent)
    }
}