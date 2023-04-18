package net.casualuhc.arcade.resources

import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.name

class PathPack(val path: Path) : ReadablePack {
    override val name: String
        get() = this.path.name

    override fun readable(): Boolean {
        return this.path.exists()
    }

    override fun length(): Long {
        return this.path.fileSize()
    }

    override fun stream(): InputStream {
        return this.path.inputStream()
    }
}