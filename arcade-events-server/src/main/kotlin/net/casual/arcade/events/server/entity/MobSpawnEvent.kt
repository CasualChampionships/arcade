/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.entity

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.SpawnGroupData

public data class MobSpawnEvent(
    override val level: ServerLevel,
    override val entity: Mob,
    val data: SpawnGroupData?
): CancellableEvent.Default(), EntityEvent