package net.casual.arcade.resources

fun interface PackSupplier {
    fun getPacks(): Iterable<ReadablePack>
}