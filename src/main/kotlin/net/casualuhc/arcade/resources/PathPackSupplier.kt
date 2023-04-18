package net.casualuhc.arcade.resources

import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

class PathPackSupplier(
    private val directory: Path
): PackSupplier {
    override fun getPacks(): Iterable<ReadablePack> {
        return this.directory.listDirectoryEntries("*.zip").map { PathPack(it) }
    }
}