package net.casual.arcade.host.pack

import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.*

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