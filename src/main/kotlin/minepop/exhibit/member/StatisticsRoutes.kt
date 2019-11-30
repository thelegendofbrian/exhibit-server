package minepop.exhibit.member

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import minepop.exhibit.auth.exhibitSession
import minepop.exhibit.group.GroupDAO
import minepop.exhibit.stats.StatsDAO
import minepop.exhibit.stats.calculateStatistics

private val groupDAO = GroupDAO()
private val statsDAO = StatsDAO()

fun Route.memberStatisticsRoutes() {
    get("/") {
        val groupId = call.parameters["groupId"]!!.toLong()
        val userId = exhibitSession().userId
        val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!
        val stats = statsDAO.retrieveStats(groupMemberId)
        val calcStats = stats.calculateStatistics()
        call.respond(calcStats)
    }
}