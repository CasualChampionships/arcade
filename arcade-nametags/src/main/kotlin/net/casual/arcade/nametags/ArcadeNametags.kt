/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags

import net.casual.arcade.nametags.extensions.EntityNametagExtension
import net.casual.arcade.nametags.virtual.NametagElementHolder
import net.fabricmc.api.ModInitializer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

public object ArcadeNametags: ModInitializer {
    private var provider: (Entity) -> NametagElementHolder? = this::createDefaultNametagElementHolder

    override fun onInitialize() {
        EntityNametagExtension.registerEvents()
    }

    @JvmStatic
    public fun setNametagElementHolderProvider(provider: (Entity) -> NametagElementHolder?) {
        this.provider = provider
    }

    internal fun createNametagElementHolder(entity: Entity): NametagElementHolder? {
        return this.provider.invoke(entity)
    }

    private fun createDefaultNametagElementHolder(entity: Entity): NametagElementHolder? {
        if (entity is ServerPlayer) {
            return NametagElementHolder(entity)
        }
        return null
    }
}