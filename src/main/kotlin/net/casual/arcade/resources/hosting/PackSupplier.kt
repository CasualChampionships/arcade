package net.casual.arcade.resources.hosting

/**
 * This interface provides a method for getting
 * multiple [ReadablePack]s to supply to a [PackHost].
 *
 * @see PackHost
 * @see ReadablePack
 */
public fun interface PackSupplier {
    /**
     * This gets all the currently available packs.
     *
     * @return The available [ReadablePack]s.
     */
    public fun getPacks(): Iterable<ReadablePack>

    public companion object {
        /**
         * Creates a [PackSupplier] from a given number of [ReadablePack]s.
         *
         * @param packs The packs to supply.
         * @return A created [PackSupplier] to supply the given packs.
         */
        @JvmStatic
        public fun of(vararg packs: ReadablePack): PackSupplier {
            val list = packs.toList()
            return PackSupplier { list }
        }
    }
}