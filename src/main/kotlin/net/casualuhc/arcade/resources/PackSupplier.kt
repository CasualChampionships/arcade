package net.casualuhc.arcade.resources

fun interface PackSupplier {
    fun getPacks(): Iterable<ReadablePack>
}