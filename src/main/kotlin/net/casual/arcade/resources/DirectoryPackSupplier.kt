package net.casual.arcade.resources

import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

/**
 * A [PackSupplier] implementation that supplies all the zipped
 * packs in a given directory.
 *
 * @param directory The directory containing all the zipped packs.
 *
 * @see PackSupplier
 * @see PathPack
 */
class DirectoryPackSupplier(
    /**
     * The directory containing all the zipped packs.
     */
    private val directory: Path
): PackSupplier {
    /**
     * This gets all the currently available packs.
     *
     * @return The available [ReadablePack]s.
     */
    override fun getPacks(): Iterable<ReadablePack> {
        return this.directory.listDirectoryEntries("*.zip").map { PathPack(it) }
    }
}