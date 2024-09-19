package net.casual.arcade.host.pack

import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

/**
 * A [ReadablePackSupplier] implementation that supplies all the zipped
 * packs in a given directory.
 *
 * @param directory The directory containing all the zipped packs.
 *
 * @see ReadablePackSupplier
 * @see PathPack
 */
public class DirectoryPackSupplier(
    /**
     * The directory containing all the zipped packs.
     */
    private val directory: Path
): ReadablePackSupplier {
    /**
     * This gets all the currently available packs.
     *
     * @return The available [ReadablePack]s.
     */
    override fun getPacks(): Iterable<ReadablePack> {
        return this.directory.listDirectoryEntries("*.zip").map { PathPack(it) }
    }
}