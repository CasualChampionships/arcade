/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random

public fun <T> ResourceLocation.toKey(registryKey: ResourceKey<out Registry<T>>): ResourceKey<T> {
    return ResourceKey.create(registryKey, this)
}

public fun ResourceKey<*>.toIdString(): String {
    return this.location().toString()
}

public fun <T> Holder<T>.isOf(tag: TagKey<T>): Boolean {
    return this.`is`(tag)
}

public fun <T> Holder<T>.isOf(location: ResourceLocation): Boolean {
    return this.`is`(location)
}

public fun <T> Holder<T>.isOf(key: ResourceKey<T>): Boolean {
    return this.`is`(key)
}

public fun DamageSource.isOf(tag: TagKey<DamageType>): Boolean {
    return this.typeHolder().isOf(tag)
}

public fun DamageSource.isOf(key: ResourceKey<DamageType>): Boolean {
    return this.typeHolder().isOf(key)
}

public fun Holder.Reference<*>.id(): ResourceLocation {
    return this.key().location()
}

public fun <T> ResourceKey<T>.getIntId(access: RegistryAccess): Int {
    val registry = access.lookup(this.registryKey()).getOrNull() ?: return -1
    return registry.getId(registry.getValue(this))
}

public fun <T> Holder.Reference<T>.getIntId(access: RegistryAccess): Int {
    val registry = access.lookup(this.key().registryKey()).getOrNull() ?: return -1
    return registry.getId(this.value())
}

public fun <T> Registry<T>.getRandomSequence(random: Random = Random): Sequence<Holder.Reference<T>> {
    val universe = this.registryKeySet().toMutableSet()
    if (universe.isEmpty()) {
        return emptySequence()
    }
    return sequence {
        while (universe.isNotEmpty()) {
            val key = universe.random(random)
            universe.remove(key)
            yield(get(key).orElseThrow())
        }
    }
}