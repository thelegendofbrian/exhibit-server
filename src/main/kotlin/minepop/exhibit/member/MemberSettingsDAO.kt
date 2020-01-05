package minepop.exhibit.member

import minepop.exhibit.dao.DAO

class MemberSettingsDAO: DAO() {

    fun retrieveMemberView(groupMemberId: Long, viewName: String): MemberSettingsView {
        val view = MemberSettingsView()
        connect().use { c ->
            c.prepareStatement("select stat.name from member_settings_view" +
                    " inner join statistic_view stat_view on stat_view.id = view_id" +
                    " inner join statistic stat on stat.id = stat_id" +
                    " where group_member_id = ? and stat_view.name = ?").use {
                it.setLong(1, groupMemberId)
                it.setString(2, viewName)
                val rs = it.executeQuery()
                while (rs.next()) {
                    view.stats += rs.getString(1)
                }
            }
        }
        return view
    }

    fun createUpdateMemberSettingsView(groupMemberId: Long, viewName: String, view: MemberSettingsView) {
        connect().use { c ->
            c.prepareStatement("delete from member_settings_view where group_member_id = ? and view_id in (select id from statistic_view where name = ?)").use {
                it.setLong(1, groupMemberId)
                it.setString(2, viewName)
                it.executeUpdate()
            }
            c.prepareStatement("insert into member_settings_view(group_member_id, view_id, stat_id) values(?, " +
                    "(select id from statistic_view where name = ?), " +
                    "(select id from statistic where name = ? and view_id = (select id from statistic_view where name = ?)))").use {
                    ps ->

                view.stats.forEach {
                    ps.setLong(1, groupMemberId)
                    ps.setString(2, viewName)
                    ps.setString(3, it)
                    ps.setString(4, viewName)
                    ps.addBatch()
                }
                ps.executeBatch()
            }
        }
    }
}