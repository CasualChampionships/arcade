package net.casual.arcade.events.entity

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

/**
 * Supports PRE, POST.
 */
public data class EntityDeathEvent(
    override val entity: LivingEntity,
    val source: DamageSource
): EntityEvent