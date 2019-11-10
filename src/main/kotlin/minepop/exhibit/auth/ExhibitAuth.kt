package minepop.exhibit.auth

import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.form
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import minepop.exhibit.Crypto
import minepop.exhibit.ExhibitSession
import minepop.exhibit.prod
import java.util.*

private val dao = SessionAuthDAO()

fun Authentication.Configuration.installExhibitAuth() {
    form(name = "Form") {
        userParamName = "username"
        passwordParamName = "password"
        skipWhen { it.sessions.get<ExhibitSession>() != null }
        validate { credentials ->

            val quickAuth = request.cookies["Quick-Auth"]
            if (quickAuth != null) {
                val userName = dao.retrieveUserForQuickAuth(quickAuth)
                if (userName != null) {
                    sessions.set(ExhibitSession(credentials.name))
                    return@validate UserIdPrincipal(credentials.name)
                }
            }

            val user = dao.retrieveUser(credentials.name)
            if (user == null || user.failedLogins >= 5) {
                return@validate null
            }

            val digest = Crypto.hash(credentials.password.toCharArray(), user.salt)
            if (digest.contentEquals(user.saltedHash)) {
                sessions.set(ExhibitSession(credentials.name))
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