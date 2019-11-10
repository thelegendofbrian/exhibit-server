package minepop.exhibit

import java.nio.file.Paths

class AppConfig : Config(Paths.get("exhibit-server.properties")) {

    override fun populate() {
        props.computeIfAbsent(host) { "" }
        populate(port, if (prod) "443" else "80") { it.testInt() }
        populate(sessionTimeout, "14400") { it.testInt() }
        props.computeIfAbsent(keystorePath) { "" }
    }

    fun getHost(): String {
        return props.getProperty(host)
    }

    fun getPort(): Int {
        return getInt(port)
    }

    fun getSessionTimeout(): Int {
        return getInt(sessionTimeout)
    }

    fun getKeystorePath(): String {
        return props.getProperty(keystorePath)
    }

    companion object {
        private const val host = "host"
        private const val port = "port"
        private const val sessionTimeout = "sessionTimeout"
        private const val keystorePath = "keystorePath"
    }
}
