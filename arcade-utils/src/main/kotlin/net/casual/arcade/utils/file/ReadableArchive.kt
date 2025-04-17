package net.casual.arcade.utils.file

import com.mojang.serialization.Decoder
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import net.minecraft.util.GsonHelper
import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.nameWithoutExtension

public interface ReadableArchive: AutoCloseable {
    public val name: String

    public fun resolve(other: String): Path

    public companion object {
        public fun from(path: Path): ReadableArchive {
            if (path.extension == "zip") {
                val system = FileSystems.newFileSystem(path)
                val root = system.getPath("/")
                return ReadablePathArchive(path.nameWithoutExtension, root) { system.close() }
            }
            return ReadablePathArchive(path.nameWithoutExtension, path)
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

        public fun <A> ReadableArchive.parseJson(path: String, decoder: Decoder<A>): Result<A> {
            return this.parse(path, decoder) {
                Dynamic(JsonOps.INSTANCE, GsonHelper.parse(it.reader()))
            }
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