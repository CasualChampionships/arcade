package net.casualuhc.arcade.resources

import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

class LocalResourcePackHost(
    private val packDirectory: Path
): ResourcePackHost() {
    override fun getPacks(): Iterable<ReadablePack> {
        return this.packDirectory.listDirectoryEntries("*.zip").map { ReadablePathPack(it) }
    }
}