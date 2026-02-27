/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.file

import com.mojang.serialization.Decoder
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import kotlinx.io.files.FileNotFoundException
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.serialization.createSerializationContext
import net.minecraft.core.HolderLookup
import net.minecraft.server.MinecraftServer
import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*

public interface ReadableArchive: AutoCloseable {
    public val name: String

    public fun resolve(other: String): Path

    public companion object {
        public fun zip(path: Path): ReadableArchive {
            if (path.isReadable()) {
                val system = FileSystems.newFileSystem(path)
                val root = system.getPath("/")
                return ReadablePathArchive(path.nameWithoutExtension, root) { system.close() }
            }
            throw FileNotFoundException("No zip at $path")
        }

        public fun from(path: Path, zipped: Boolean = false): ReadableArchive {
            if (zipped || path.extension == "zip") {
                return this.zip(path)
            }
            if (path.isDirectory()) {
                return ReadablePathArchive(path.nameWithoutExtension, path)
            }
            throw FileNotFoundException("No archive at $path")
        }

        public fun from(path: Path, name: String): ReadableArchive {
            val zip = path.resolve("$name.zip")
            val dir = path.resolve(name)
            return when {
                zip.isReadable() -> this.from(zip)
                dir.isDirectory() -> this.from(dir)
                else -> throw FileNotFoundException("No archive at $dir")
            }
        }

        public fun ReadableArchive.child(path: String): ReadableArchive {
            return from(this.resolve("."), path)
        }

        public fun <A> ReadableArchive.parse(
            path: String,
            decoder: Decoder<A>,
            converter: (InputStream) -> Dynamic<*>
        ): Result<A> {
            return runCatching {
                val file = this.resolve(path)
                decoder.parse(file.inputStream().use(converter)).partialOrThrow
            }
        }

        public fun <A> ReadableArchive.parseJson(path: String, decoder: Decoder<A>, lookup: HolderLookup.Provider? = null): Result<A> {
            return this.parse(path, decoder) {
                Dynamic(lookup.createSerializationContext(JsonOps.INSTANCE), JsonUtils.decodeRaw(it.reader()))
            }
        }

        public fun <A> ReadableArchive.parseJson(path: String, decoder: Decoder<A>, server: MinecraftServer): Result<A> {
            return this.parseJson(path, decoder, server.registryAccess())
        }
    }

    private class ReadablePathArchive(
        override val name: String,
        private val root: Path,
        private val closer: () -> Unit = { }
    ): ReadableArchive {
        override fun resolve(other: String): Path {
            return this.root.resolve(other)
        }

        override fun close() {
            this.closer.invoke()
        }
    }
}