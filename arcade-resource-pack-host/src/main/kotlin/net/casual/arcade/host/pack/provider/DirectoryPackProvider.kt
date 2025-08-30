/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host.pack.provider

import net.casual.arcade.host.pack.PathPack
import net.casual.arcade.host.pack.ReadablePack
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile

/**
 * A [PackProvider] implementation that supplies all the zipped
 * packs in a given directory.
 *
 * @param directory The directory containing all the zipped packs.
 */
public class DirectoryPackProvider(
    /**
     * The directory containing all the zipped packs.
     */
    private val directory: Path
): PackProvider {
    /**
     * Gets the pack with a given [name], only packs
     * within the current directory are allowed.
     *
     * @param name The name of the pack.
     * @return The provided pack.
     */
    override fun get(name: String): ReadablePack? {
        val base = this.directory.toAbsolutePath().normalize()
        val user = base.resolve("$name.zip").normalize()
        if (user.parent == base && user.extension == "zip" && user.isRegularFile()) {
            return PathPack(user)
        }
        return null
    }
}