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
import minepop.exhibit.checkin.CheckinDAO
import minepop.exhibit.group.Group
import minepop.exhibit.group.GroupDAO
import minepop.exhibit.prod
import minepop.exhibit.stats.GroupMemberCalculatedStatistics
import minepop.exhibit.stats.StatsDAO
import minepop.exhibit.stats.calculateStatistics
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

val groupDAO = GroupDAO()
val statsDAO = StatsDAO()
val checkinDAO = CheckinDAO()

data class LoginResponse(val user: LoginResponseUser)
data class LoginResponseUser(val name: String, val groups: List<Group>, val settings: LoginResponseUserSettings)
data class LoginResponseUserSettings(val timeZone: String, val defaultGroupId: Long?, val displayName: String?,
                                     val userStatsToDisplay: List<GroupStats>, val defaultGroupStats: GroupMemberCalculatedStatistics?,
                                     val isDefaultGroupCheckedIn: Boolean?)
data class GroupStats(val groupId: Long, val stats: List<String>)

fun Route.authRoutes() {
    post("login") {
        val groups = groupDAO.retrieveGroups(exhibitSession().userid)
        val groupStats = mutableListOf<GroupStats>()
        groupStats += GroupStats(1, listOf("dayStreak", "adherencePercent", "points"))
        var defaultGroupStats: GroupMemberCalculatedStatistics? = null
        var isDefaultGroupCheckedIn: Boolean? = null
        exhibitSession().defaultGroupId?.let {
            val groupMemberId = groupDAO.retrieveGroupMemberId(exhibitSession().userid, it)!!
            val stats = statsDAO.retrieveStats(groupMemberId)
            defaultGroupStats = stats.calculateStatistics()
            val defaultGroupCheckin = checkinDAO.retrieveGroupMemberCheckins(groupMemberId, exhibitSession().currentDate(), 1)
            isDefaultGroupCheckedIn = !defaultGroupCheckin.isEmpty()
        }
        val settings = LoginResponseUserSettings(exhibitSession().timezone, exhibitSession().defaultGroupId,
            "Hardcoded Display Name", groupStats, defaultGroupStats, isDefaultGroupCheckedIn)
        val user = LoginResponseUser(exhibitSession().username, groups, settings)
        call.respond(LoginResponse(user))
    }
    post("logout") {
        call.sessions.clear<ExhibitSession>()
        call.response.cookies.appendExpired("Quick-Auth")
        call.respond(HttpStatusCode.NoContent)
    }
}