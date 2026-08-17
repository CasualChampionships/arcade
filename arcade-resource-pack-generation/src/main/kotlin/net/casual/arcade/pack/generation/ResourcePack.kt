/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.generation

import net.casual.arcade.pack.host.ReadablePack
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.writeBytes

public class ResourcePack internal constructor(
    override val name: String,
    private val bytes: ByteArray,
    private val hash: String
): ReadablePack {
    public val zipped: String
        get() = "${this.name}.zip"

    override fun stream(): InputStream {
        return ByteArrayInputStream(this.bytes)
    }

    override fun length(): Long {
        return this.bytes.size.toLong()
    }

    override fun hash(): String {
        return this.hash
    }

    public fun writeTo(directory: Path): Path {
        if (directory.exists() && !directory.isDirectory()) {
            throw IllegalArgumentException("Must specify a directory to write a resource pack to, got $directory")
        }
        directory.createDirectories()
        val path = directory.resolve(this.zipped)
        path.writeBytes(this.bytes)
        return path
    }
}
