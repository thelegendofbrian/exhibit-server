package minepop.exhibit.dao

import minepop.exhibit.Config
import minepop.exhibit.testInt
import java.nio.file.Paths

class DAOConfig : Config(Paths.get("datasource.properties")) {

    override fun populate() {
        props.computeIfAbsent(host) { "" }
        props.computeIfAbsent(user) { "exhibit" }
        props.computeIfAbsent(passwd) { "" }
        props.computeIfAbsent(schema) { "exhibit" }
        populate(port, "3306") { it.testInt() }
        populate(connectionPool, "3") { it.testInt() }
    }

    fun getHost(): String {
        return props.getProperty(host)
    }

    fun getPort(): Int {
        return getInt(port)
    }

    fun getUser(): String {
        return props.getProperty(user)
    }

    fun getPasswd(): String {
        return props.getProperty(passwd)
    }

    fun getSchema(): String {
        return props.getProperty(schema)
    }

    fun getConnectionPool(): Int {
        return getInt(connectionPool)
    }

    companion object {

        private const val host = "host"
        private const val port = "port"
        private const val user = "user"
        private const val passwd = "passwd"
        private const val schema = "schema"
        private const val connectionPool = "connectionPool"
    }
}
