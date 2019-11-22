package minepop.exhibit.group

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import minepop.exhibit.auth.exhibitSession

val groupDAO = GroupDAO()

fun Route.groupRoutes() {
    route("group") {
        get("/") {
            val contains = call.request.queryParameters["contains"]!!
            val limit = call.request.queryParameters["limit"]!!.toInt()
            val groups = groupDAO.retrieveGroups(contains, limit)
            call.respond(groups)
        }
        post("/") {
            val postGroup = call.receive<PostGroup>()
            val group = groupDAO.createUpdateGroup(exhibitSession().userid, postGroup)
            call.respond(group!!)
        }
        delete("/") {
            groupDAO.deleteGroup(call.request.queryParameters["groupId"]!!.toLong())
            call.respond(HttpStatusCode.NoContent)
        }
        route("member/{groupId}") {
            post("/") {
                val groupId = call.parameters["groupId"]!!.toLong()
                groupDAO.createGroupMember(groupId, exhibitSession().userid)
                call.respond(HttpStatusCode.NoContent)
            }
            delete("/") {
                val groupId = call.parameters["groupId"]!!.toLong()
                groupDAO.deleteGroupMember(groupId, exhibitSession().userid)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}