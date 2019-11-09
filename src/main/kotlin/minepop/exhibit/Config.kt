package minepop.exhibit

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

abstract class Config(private val path: Path) {

    protected val props = Properties()

    init {
        load()
        populate()
        save()
    }

    private fun load() {
        try {
            Files.newInputStream(path).use { props.load(it) }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    protected abstract fun populate()

    fun getInt(prop: String): Int {
        return props.getProperty(prop).toInt()
    }

    protected fun populate(prop: String, defaultValue: String, test: (prop: String) -> Boolean) {
        val obj = props.computeIfAbsent(prop) { defaultValue }
        if (!test(obj.toString()))
            props.setProperty(prop, defaultValue)
    }

    private fun save() {
        try {
            Files.newOutputStream(path).use { props.store(it, null) }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}

fun String.testInt(): Boolean {
    try {
        this.toInt()
    } catch (ex: NumberFormatException) {
        ex.printStackTrace()
        return false
    }
    return true
}