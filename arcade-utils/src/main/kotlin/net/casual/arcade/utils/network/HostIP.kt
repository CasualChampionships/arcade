/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.network

import net.casual.arcade.utils.ArcadeUtils
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines
import kotlin.io.path.writeText

public object HostIP {
    internal const val LOCALHOST = "127.0.0.1"

    private val DEFAULT_CONFIG = """
    # Enter the server ip here:
    $LOCALHOST
    """.trimIndent()

    private val value = this.read()

    public fun get(): String? {
        return this.value
    }

    public fun getOrDefault(): String {
        return this.value ?: LOCALHOST
    }

    private fun read(): String? {
        val path = ArcadeUtils.path.resolve("host-ip.txt")
        if (path.isRegularFile()) {
            val ip = path.readLines().firstOrNull { it.isNotBlank() && !it.startsWith('#') }?.trim()
            if (ip != null) {
                if (ip != LOCALHOST) {
                    return ip
                }
                return null
            }
        }
        try {
            path.writeText(DEFAULT_CONFIG)
        } catch (exception: Exception) {
            ArcadeUtils.logger.error("Failed to write host ip config", exception)
        }
        return null
    }
}