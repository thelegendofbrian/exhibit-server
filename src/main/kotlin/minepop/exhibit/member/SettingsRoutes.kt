package minepop.exhibit.member

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import minepop.exhibit.auth.exhibitSession
import minepop.exhibit.group.GroupDAO

private val groupDAO = GroupDAO()
private val memberSettingsDAO = MemberSettingsDAO()

fun Route.settingsRoutes() {
    get("/") {
        val groupId = call.parameters["groupId"]!!.toLong()
        val userId = exhibitSession().userId
        val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!
        val viewName = call.parameters["view"]!!

        val view = memberSettingsDAO.retrieveMemberView(groupMemberId, viewName)

        val viewResponse = JsonObject()
        val stats = JsonArray()
        view.stats.forEach {
            stats.add(it)
        }
        viewResponse.add("stats", stats)

        call.respond(viewResponse)
    }
    post("/") {
        val groupId = call.parameters["groupId"]!!.toLong()
        val userId = exhibitSession().userId
        val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!
        val viewName = call.parameters["view"]!!

        val view = MemberSettingsView()
        val request = call.receive<JsonObject>()
        request.getAsJsonArray("stats").forEach {
            view.stats += it.asString
        }

        memberSettingsDAO.createUpdateMemberSettingsView(groupMemberId, viewName, view)
        call.respond(HttpStatusCode.NoContent)
    }
}