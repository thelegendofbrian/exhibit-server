package minepop.exhibit.group

import minepop.exhibit.dao.DAO

class GroupDAO: DAO() {

    fun retrieveGroups(userName: String): List<String> {
        val groups = mutableListOf<String>()
        connect().use { c ->
            c.prepareStatement("select group.name from group_member" +
                    " inner join `group` on group_id = group.id inner join user on user_id = user.id where user.name = ?").use {
                it.setString(1, userName)
                val rs = it.executeQuery()
                while (rs.next())
                    groups += rs.getString(1)
            }
        }
        return groups
    }

    fun retrieveMembers(groupName: String): List<String> {
        val members = mutableListOf<String>()
        connect().use { c ->
            c.prepareStatement("select group.name from group_member" +
                    " inner join `group` on group_id = group.id inner join user on user_id = user.id where group.name = ?").use {
                it.setString(1, groupName)
                val rs = it.executeQuery()
                while (rs.next())
                    members += rs.getString(1)
            }
        }
        return members
    }
}