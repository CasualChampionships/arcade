package net.casual.arcade.utils

public object StringUtils {
    public fun String.decodeHexToBytes(): ByteArray {
        return this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    public fun ByteArray.encodeToHexString(): String {
        return this.joinToString("") { "%02x".format(it) }
    }
}