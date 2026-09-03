package ram.talia.hexal.api.everbook

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/** AES wrapper retained for compatibility with Hexal's original cross-world Everbook files. */
internal class FileEncrypterDecrypter(private val secretKey: SecretKey) {
    private val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    fun encrypt(content: CompoundTag, file: File) {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        file.absoluteFile.parentFile.mkdirs()
        FileOutputStream(file).use { output ->
            output.write(cipher.iv)
            NbtIo.writeCompressed(content, CipherOutputStream(output, cipher))
        }
    }

    fun decryptCompound(file: File, maxBytes: Long): CompoundTag? {
        if (!file.exists()) return null
        return runCatching {
            FileInputStream(file).use { input ->
                val iv = ByteArray(16)
                if (input.read(iv) != iv.size) return null
                cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
                NbtIo.readCompressed(CipherInputStream(input, cipher), NbtAccounter.create(maxBytes))
            }
        }.getOrNull()
    }

    companion object {
        fun forUuid(uuid: UUID): FileEncrypterDecrypter {
            val buffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES)
            buffer.putLong(uuid.mostSignificantBits)
            buffer.putLong(uuid.leastSignificantBits)
            return FileEncrypterDecrypter(SecretKeySpec(buffer.array(), "AES"))
        }
    }
}
