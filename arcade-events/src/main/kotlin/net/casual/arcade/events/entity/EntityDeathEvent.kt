package net.casual.arcade.events.entity

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