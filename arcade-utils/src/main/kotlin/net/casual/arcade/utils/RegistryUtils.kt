package net.casual.arcade.utils

import net.minecraft.core.Holder
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType

public fun <T> Holder<T>.isOf(tag: TagKey<T>): Boolean {
    return this.`is`(tag)
}

public fun <T> Holder<T>.isOf(location: ResourceLocation): Boolean {
    return this.`is`(location)
}

public fun <T> Holder<T>.isOf(key: ResourceKey<T>): Boolean {
    return this.`is`(key)
}

public fun DamageSource.isOf(tag: TagKey<DamageType>) {
    this.typeHolder().isOf(tag)
}

public fun DamageSource.isOf(key: ResourceKey<DamageType>) {
    this.typeHolder().isOf(key)
}