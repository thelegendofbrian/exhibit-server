package minepop.exhibit.group

import minepop.exhibit.dao.DAO
import minepop.exhibit.user.User
import java.sql.PreparedStatement

class GroupDAO: DAO() {

    fun retrieveGroups(userName: String): List<Group> {
        val groups = mutableListOf<Group>()
        connect().use { c ->
            c.prepareStatement("select group.name, group_id, owner_user_id from group_member" +
                    " inner join `group` on group_id = group.id inner join user on user_id = user.id where user.name = ?").use {
                it.setString(1, userName)
                val rs = it.executeQuery()
                while (rs.next())
                    groups += Group(rs.getLong(2), rs.getString(1), rs.getLong(3))
            }
        }
        return groups
    }

    fun retrieveGroups(contains: String?, limit: Int): List<Group> {
        val groups = mutableListOf<Group>()
        connect().use { c ->
            var sql = "select id, name, owner_user_id from `group`"
            contains.let {
                sql += " where name like '%?%'"
            }
            sql += " limit ?"
            c.prepareStatement(sql).use { ps ->
                contains.let {
                    ps.setString(1, it)
                }
                ps.setInt(if (contains == null) 1 else 2, limit)
                val rs = ps.executeQuery()
                while (rs.next())
                    groups += Group(rs.getLong(1), rs.getString(2), rs.getLong(3))
            }
        }
        return groups
    }

    fun retrieveMembers(groupName: String): List<User> {
        val members = mutableListOf<User>()
        connect().use { c ->
            c.prepareStatement("select user_id, user.name from group_member" +
                    " inner join user on user_id = user.id where group_id = ?").use {
                it.setString(1, groupName)
                val rs = it.executeQuery()
                while (rs.next())
                    members += User(rs.getLong(1), rs.getString(2))
            }
        }
        return members
    }

    fun createUpdateGroup(ownerUserId: Long, group: PostGroup): Group? {
        connect().use { c ->
            if (group.id == null) {
                c.prepareStatement("insert into `group`(name, owner_user_id) values (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS).use {
                    it.setString(1, group.name)
                    it.setLong(2, ownerUserId)
                    it.executeUpdate()
                    val generatedKeys = it.generatedKeys
                    if (generatedKeys.next()) {
                        return Group(generatedKeys.getLong(1), group.name, ownerUserId)
                    }
                }
            } else {
                c.prepareStatement("update `group` set name = ? where id = ?").use {
                    it.setString(1, group.name)
                    it.setLong(2, group.id!!)
                    it.executeUpdate()
                    return Group(group.id!!, group.name, ownerUserId)
                }
            }
        }
        return null
    }

    fun deleteGroup(groupId: Long) {
        connect().use { c ->
            c.prepareStatement("delete from `group` where id = ?").use {
                it.setLong(1, groupId)
                it.executeUpdate()
            }
        }
    }
}