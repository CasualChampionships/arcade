/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.debug.behavior

import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.DebugEntityNameGenerator
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities

public object BehaviorDescriptionOverrides {
    private val overrides = HashMap<Class<*>, (Any) -> String>()

    @JvmStatic
    public fun get(value: Any): String? {
        val mapper = this.overrides[value.javaClass] ?: return null
        return mapper.invoke(value)
    }

    @JvmStatic
    public fun <T: Any> register(type: Class<T>, mapper: (T) -> String) {
        @Suppress("UNCHECKED_CAST")
        this.overrides[type] = mapper as (Any) -> String
    }

    public inline fun <reified T: Any> register(noinline mapper: (T) -> String) {
        this.register(T::class.java, mapper)
    }

    internal fun bootstrap() {
        this.register<BlockPos> { pos -> "(${pos.x}, ${pos.y}, ${pos.z})" }
        this.register<NearestVisibleLivingEntities> { entities ->
            entities.findAll { true }.joinToString(transform = DebugEntityNameGenerator::getEntityName)
        }
    }
}