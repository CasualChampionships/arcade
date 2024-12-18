/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.entity

import net.casual.arcade.events.BuiltInEventPhases
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

public data class EntityDeathEvent(
    override val entity: LivingEntity,
    val source: DamageSource
): EntityEvent {
    public companion object {
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}