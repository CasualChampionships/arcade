/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host.pack

import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.isReadable
import kotlin.io.path.name

public class PathPack(private val path: Path): ReadablePack {
    override val name: String
        get() = this.path.name

    override fun stream(): InputStream {
        return this.path.inputStream()
    }

    override fun readable(): Boolean {
        return this.path.isReadable()
    }

    override fun length(): Long {
        return this.path.fileSize()
    }
}