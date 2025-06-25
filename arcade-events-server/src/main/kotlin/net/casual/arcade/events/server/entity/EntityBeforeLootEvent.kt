/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.entity

import net.minecraft.world.entity.Entity
import net.minecraft.world.level.storage.loot.LootParams

public data class EntityBeforeLootEvent(
    override val entity: Entity,
    val params: LootParams,
    var lootMultiplier: Int
): EntityEvent