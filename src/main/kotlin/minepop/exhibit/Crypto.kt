package minepop.exhibit

import java.security.SecureRandom

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Crypto {

    private val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    private const val IT = 98304
    private val r = SecureRandom()

    fun hash(pw: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pw, salt, IT, 256)
        return factory.generateSecret(spec).encoded
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
