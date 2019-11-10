package minepop.exhibit

import java.nio.file.Paths

class AppConfig : Config(Paths.get("exhibit-server.properties")) {

    override fun populate() {
        props.computeIfAbsent(host) { "" }
        populate(port, if (prod) "443" else "80") { it.testInt() }
        populate(sessionTimeout, "14400") { it.testInt() }
        props.computeIfAbsent(keystorePath) { "" }
        props.computeIfAbsent(keystoreAlias) { "" }
        props.computeIfAbsent(keystorePasswd) { "" }
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

    fun getKeystoreAlias(): String {
        return props.getProperty(keystoreAlias)
    }

    fun getKeystorePasswd(): String {
        return props.getProperty(keystorePasswd)
    }

    companion object {
        private const val host = "host"
        private const val port = "port"
        private const val sessionTimeout = "sessionTimeout"
        private const val keystorePath = "keystorePath"
        private const val keystoreAlias = "keystoreAlias"
        private const val keystorePasswd = "keystorePasswd"
    }
}
