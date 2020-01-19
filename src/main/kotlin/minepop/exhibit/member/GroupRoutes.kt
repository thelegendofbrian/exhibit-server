package minepop.exhibit.member

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import minepop.exhibit.auth.currentDate
import minepop.exhibit.auth.exhibitSession
import minepop.exhibit.group.GroupDAO
import minepop.exhibit.schedule.NoneSchedule
import minepop.exhibit.schedule.ScheduleDAO
import minepop.exhibit.stats.StatsDAO
import minepop.exhibit.user.UserSettingsDAO

private val groupDAO = GroupDAO()
private val userSettingsDAO = UserSettingsDAO()
private val scheduleDAO = ScheduleDAO()
private val memberSettingsDAO = MemberSettingsDAO()

fun Route.memberGroupRoutes() {
    route("{groupId}") {
        post("/") {
            val groupId = call.parameters["groupId"]!!.toLong()
            val userId = exhibitSession().userId
            groupDAO.createGroupMember(groupId, userId)

            if (groupDAO.retrieveGroupsByMember(userId).size == 1) {
                var settings = userSettingsDAO.retrieveSettings(userId)
                settings.defaultGroupId = groupId
                userSettingsDAO.updateSettings(settings)
            }

            val groupMemberId = groupDAO.retrieveGroupMemberId(groupId, userId)!!
            scheduleDAO.createUpdateSchedule(NoneSchedule(groupMemberId, exhibitSession().currentDate()))

            memberSettingsDAO.createUpdateMemberSettingsView(groupMemberId, "user", MemberSettingsView(listOf("dayStreak", "adherence", "totalCheckins")))

            call.respond(HttpStatusCode.NoContent)
        }
        delete("/") {
            val groupId = call.parameters["groupId"]!!.toLong()
            groupDAO.deleteGroupMember(groupId, exhibitSession().userId)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    get("group/") {
        val groups = groupDAO.retrieveGroupsByMember(exhibitSession().userId)
        val settings = userSettingsDAO.retrieveSettings(exhibitSession().userId)
        val response = JsonObject()
        response.addProperty("defaultGroupId", settings.defaultGroupId)
        val groupsArray = JsonArray()
        groups.forEach {
            val groupJson = JsonObject()
            groupJson.addProperty("id", it.id)
            groupJson.addProperty("name", it.name)
            groupsArray.add(groupJson)
        }
        response.add("groups", groupsArray)
        call.respond(response)
    }
}