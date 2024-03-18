package net.casual.arcade.utils

public object StringUtils {
    private val SMALL_CAPS_ALPHABET = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘqʀꜱᴛᴜᴠᴡxyᴢ".toCharArray()

    public fun String.toSmallCaps(): String {
        val builder = StringBuilder()
        for (char in this) {
            val replacement = when (char) {
                in 'a'..'z' -> SMALL_CAPS_ALPHABET[char - 'a']
                in 'A'..'Z' -> SMALL_CAPS_ALPHABET[char - 'A']
                else -> char
            }
            builder.append(replacement)
        }
        return builder.toString()
    }

    public fun String.decodeHexToBytes(): ByteArray {
        return this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    public fun ByteArray.encodeToHexString(): String {
        return this.joinToString("") { "%02x".format(it) }
    }
}