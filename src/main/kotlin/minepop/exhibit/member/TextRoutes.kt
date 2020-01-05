package minepop.exhibit.member

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import minepop.exhibit.auth.exhibitSession
import minepop.exhibit.group.GroupDAO

private val groupDAO = GroupDAO()

fun Route.textRoutes() {
    route("{type}") {
        get("/") {
            val groupId = call.parameters["groupId"]!!.toLong()
            val type = call.parameters["type"]!!
            val groupMemberId = groupDAO.retrieveGroupMemberId(exhibitSession().userId, groupId)!!

            val text = groupDAO.retrieveGroupMemberText(groupMemberId, type)

            if (text == null) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(text)
            }
        }
        post("/") {
            val groupId = call.parameters["groupId"]!!.toLong()
            val type = call.parameters["type"]!!
            val groupMemberId = groupDAO.retrieveGroupMemberId(exhibitSession().userId, groupId)!!
            val text = call.receiveText()

            groupDAO.createUpdateGroupMemberText(groupMemberId, type, text)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}