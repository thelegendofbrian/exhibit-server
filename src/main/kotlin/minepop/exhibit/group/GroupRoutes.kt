package minepop.exhibit.group

import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import minepop.exhibit.auth.exhibitSession

private val groupDAO = GroupDAO()

fun Route.groupRoutes() {
    route("group") {
        get("/") {
            val contains = call.request.queryParameters["contains"]!!
            val page = call.request.queryParameters["page"]!!.toInt()
            val pageSize = call.request.queryParameters["pageSize"]!!.toInt()
            val nonmemberOnly = call.request.queryParameters["nonmemberOnly"]!!.toBoolean()
            val groups = groupDAO.retrieveGroups(contains, page, pageSize, if (nonmemberOnly) exhibitSession().userId else null)
            call.respond(groups)
        }
        post("/") {
            val body = call.receive<JsonObject>()
            if (body.has("id")) {
                val groupId = body.get("id").asLong
                val groupName = body.get("name").asString
                groupDAO.updateGroup(Group(groupId, groupName, exhibitSession().userId))
                call.respond(HttpStatusCode.NoContent)
            } else {
                val groupId = groupDAO.createGroup(exhibitSession().userId, body.get("name").asString)
                val response = JsonObject()
                response.addProperty("id", groupId)
                call.respond(response)
            }
        }
        delete("/") {
            val groupId = call.request.queryParameters["groupId"]!!.toLong()
            groupDAO.deleteGroup(groupId, exhibitSession().userId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}