/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.generation

import com.google.gson.JsonElement
import net.casual.arcade.utils.JsonUtils
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.inputStream

/**
 * A single file within a resource pack.
 *
 * @see PackContents.addFile
 */
public interface PackFile {
    public fun stream(): InputStream

    public fun bytes(): ByteArray {
        return this.stream().use(InputStream::readBytes)
    }

    private class FromPath(private val path: Path): PackFile {
        override fun stream(): InputStream {
            return this.path.inputStream()
        }
    }

    private class RawBytes(private val bytes: ByteArray): PackFile {
        override fun stream(): InputStream {
            return ByteArrayInputStream(this.bytes)
        }
        override fun bytes(): ByteArray {
            return this.bytes
        }
    }

    public companion object {
        @JvmStatic
        public fun of(bytes: ByteArray): PackFile {
            return RawBytes(bytes)
        }

        @JvmStatic
        public fun of(path: Path): PackFile {
            return FromPath(path)
        }

        @JvmStatic
        public fun of(contents: String): PackFile {
            return RawBytes(contents.encodeToByteArray())
        }

        @JvmStatic
        public fun of(json: JsonElement): PackFile {
            return RawBytes(JsonUtils.MIN_GSON.toJson(json).encodeToByteArray())
        }

        @JvmStatic
        public fun of(image: BufferedImage): PackFile {
            return RawBytes(ByteArrayOutputStream().use { stream ->
                ImageIO.write(image, "png", stream)
                stream.toByteArray()
            })
        }
    }
}
