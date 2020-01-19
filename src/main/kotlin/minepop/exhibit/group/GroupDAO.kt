package minepop.exhibit.group

import minepop.exhibit.dao.DAO
import minepop.exhibit.user.User
import java.sql.PreparedStatement
import java.sql.Types

class GroupDAO: DAO() {

    fun retrieveGroupsByMember(userId: Long): List<Group> {
        val groups = mutableListOf<Group>()
        connect().use { c ->
            c.prepareStatement("select group.name, group_id, owner_user_id from group_member" +
                    " inner join `group` on group_id = group.id where user_id = ?").use {
                it.setLong(1, userId)
                val rs = it.executeQuery()
                while (rs.next())
                    groups += Group(rs.getLong(2), rs.getString(1), rs.getLong(3))
            }
        }
        return groups
    }

    fun retrieveGroups(contains: String?, page: Int, pageSize: Int, userId: Long?): List<Group> {
        val groups = mutableListOf<Group>()
        connect().use { c ->
            var sql = "select id, name, owner_user_id from `group`"
            contains?.let {
                sql += " where name like concat('%', ?, '%')"
            }
            userId?.let {
                sql += if (contains == null) " where" else " and"
                sql += " id not in (select group_id from group_member where user_id = ?)"
            }
            sql += " order by name limit ?,?"
            c.prepareStatement(sql).use { ps ->
                var idx = 1
                contains?.let {
                    ps.setString(idx, it)
                    idx++
                }
                userId?.let {
                    ps.setLong(idx, userId)
                    idx++
                }
                val limitStart = (page - 1) * pageSize
                ps.setInt(idx, limitStart)
                idx++
                ps.setInt(idx, pageSize)
                val rs = ps.executeQuery()
                while (rs.next())
                    groups += Group(rs.getLong(1), rs.getString(2), rs.getLong(3))
            }
        }
        return groups
    }

    fun retrieveMembers(groupId: Long): List<User> {
        val members = mutableListOf<User>()
        connect().use { c ->
            c.prepareStatement("select user_id, user.name from group_member" +
                    " inner join user on user_id = user.id where group_id = ?").use {
                it.setLong(1, groupId)
                val rs = it.executeQuery()
                while (rs.next())
                    members += User(rs.getLong(1), rs.getString(2))
            }
        }
        return members
    }

    fun createGroup(ownerUserId: Long, groupName: String): Long {
        connect().use { c ->
            c.prepareStatement("insert into `group`(name, owner_user_id) values (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS).use {
                it.setString(1, groupName)
                it.setLong(2, ownerUserId)
                it.executeUpdate()
                val generatedKeys = it.generatedKeys
                generatedKeys.next()
                return generatedKeys.getLong(1)
            }
        }
    }

    fun updateGroup(group: Group) {
        connect().use { c ->
            c.prepareStatement("update `group` set name = ? where id = ? and owner_user_id = ?").use {
                it.setString(1, group.name)
                it.setLong(2, group.id)
                it.setLong(3, group.ownerUserId)
                it.executeUpdate()
            }
        }
    }

    fun deleteGroup(groupId: Long, userId: Long) {
        connect().use { c ->
            c.prepareStatement("delete from `group` where id = ? and owner_user_id = ?").use {
                it.setLong(1, groupId)
                it.setLong(2, userId)
                it.executeUpdate()
            }
        }
    }

    fun createGroupMember(groupId: Long, userId: Long) {
        connect().use { c ->
            var groupMemberId: Long? = null
            c.prepareStatement("insert into group_member(group_id, user_id) values(?, ?)", PreparedStatement.RETURN_GENERATED_KEYS).use {
                it.setLong(1, groupId)
                it.setLong(2, userId)
                it.executeUpdate()
                val keys = it.generatedKeys
                if (keys.next()) {
                    groupMemberId = keys.getLong(1)
                }
            }
            c.prepareStatement("insert into group_member_stats(group_member_id) values(?)").use {
                it.setLong(1, groupMemberId!!)
                it.executeUpdate()
            }
            c.prepareStatement("insert into group_member_stats_state(group_member_id, status_id) values(?, (select id from status where description = ?))").use {
                it.setLong(1, groupMemberId!!)
                it.setString(2, "Ready")
                it.executeUpdate()
            }
        }
    }

    fun deleteGroupMember(groupId: Long, userId: Long) {
        connect().use { c ->
            c.prepareStatement("delete group_member where group_id = ? and user_id = ?").use {
                it.setLong(1, groupId)
                it.setLong(2, userId)
                it.executeUpdate()
            }
        }
    }

    fun retrieveGroupMemberId(groupId: Long, userId: Long): Long? {
        connect().use { c ->
            c.prepareStatement("select id from group_member where group_id = ? and user_id = ?").use {
                it.setLong(1, groupId)
                it.setLong(2, userId)
                val rs = it.executeQuery()
                if (rs.next()) {
                    return rs.getLong(1)
                }
            }
        }
        return null
    }

    fun createUpdateGroupMemberText(groupMemberId: Long, type: String, text: String?) {
        connect().use { c ->
            var textId: Long? = null
            c.prepareStatement("select id from group_member_text where group_member_id = ? and type = ?").use {
                it.setLong(1, groupMemberId)
                it.setString(2, type)
                val rs = it.executeQuery()
                if (rs.next()) {
                    textId = rs.getLong(1)
                }
            }
            if (textId == null) {
                c.prepareStatement("insert into group_member_text(group_member_id, type, text) values (?, ?, ?)").use {
                    it.setLong(1, groupMemberId)
                    it.setString(2, type)
                    if (text == null) {
                        it.setNull(3, Types.VARCHAR)
                    } else {
                        it.setString(3, text)
                    }
                    it.executeUpdate()
                }
            } else {
                c.prepareStatement("update group_member_text set text = ? where id = ?").use {
                    if (text == null) {
                        it.setNull(1, Types.VARCHAR)
                    } else {
                        it.setString(1, text)
                    }
                    it.setLong(2, textId!!)
                    it.executeUpdate()
                }
            }
        }
    }

    fun retrieveGroupMemberText(groupMemberId: Long, type: String): String? {
        connect().use { c ->
            c.prepareStatement("select text from group_member_text where group_member_id = ? and type = ?").use {
                it.setLong(1, groupMemberId)
                it.setString(2, type)
                val rs = it.executeQuery()
                return if (rs.next()) rs.getString(1) else null
            }
        }
    }
}