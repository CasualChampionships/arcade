/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

private val SMALL_CAPS_ALPHABET = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘqʀꜱᴛᴜᴠᴡxyᴢ".toCharArray()

private val UUID_REGEX = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

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

public fun String.isUUID(): Boolean {
    return this.matches(UUID_REGEX)
}

public fun ByteArray.encodeToHexString(): String {
    return this.joinToString("") { "%02x".format(it) }
}