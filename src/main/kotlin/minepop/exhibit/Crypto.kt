package minepop.exhibit

import java.security.GeneralSecurityException
import java.security.SecureRandom

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Crypto {

    private val factory: SecretKeyFactory?
    private const val IT = 98304
    private val r = SecureRandom()

    init {
        var theFactory: SecretKeyFactory?
        try {
            theFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        } catch (ex: GeneralSecurityException) {
            ex.printStackTrace()
            theFactory = null
        }

        factory = theFactory
    }

    fun hash(pw: CharArray, salt: ByteArray): ByteArray {
        try {
            val spec = PBEKeySpec(pw, salt, IT, 256)
            return factory!!.generateSecret(spec).encoded
        } catch (ex: GeneralSecurityException) {
            // this will only happen if the key spec is wrong
            throw Error(ex)
        }
    }

    fun nextSalt(len: Int): ByteArray {
        val b = ByteArray(len)
        r.nextBytes(b)
        return b
    }

    fun nextInt(n: Int): Int {
        return r.nextInt(n)
    }
}
