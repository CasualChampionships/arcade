/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.entity

import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

public data class EntityDamageEvent(
    override val entity: LivingEntity,
    val source: DamageSource,
    var amount: Float,
): CancellableEvent.Default(), EntityEvent {
    public companion object {
        public const val PHASE_PRE: Int = BuiltInEventPhases.PRE

        public const val PHASE_POST: Int = BuiltInEventPhases.POST
    }
}