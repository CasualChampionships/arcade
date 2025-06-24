/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai.behavior

import net.casual.arcade.npc.FakePlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid.StopAttackCondition
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid.TargetErasedCallback
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import java.util.*

public object FakePlayerStopAttacking {
    public fun <E: FakePlayer> create(
        canStopAttacking: StopAttackCondition = StopAttackCondition { _, _ -> false },
        onStopAttacking: TargetErasedCallback<E> = TargetErasedCallback { _, _, _ -> },
        canTire: Boolean = true
    ): BehaviorControl<E> {
        return BehaviorBuilder.create { instance: BehaviorBuilder.Instance<E> ->
            instance.group(
                instance.present(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
            ).apply(instance) { targetMemoryAccessor, timeSinceInvalidMemoryAccessor ->
                Trigger { level, entity, _ ->
                    val livingEntity = instance.get(targetMemoryAccessor)
                    val canContinueAttacking = entity.canAttack(livingEntity)
                        && (!canTire || !isTired(entity, instance.tryGet(timeSinceInvalidMemoryAccessor)))
                        && livingEntity.isAlive
                        && livingEntity.level() === entity.level()
                        && !canStopAttacking.test(level, livingEntity)
                    if (!canContinueAttacking) {
                        onStopAttacking.accept(level, entity, livingEntity)
                        targetMemoryAccessor.erase()
                    }
                    true
                }
            }
        }
    }

    private fun isTired(entity: LivingEntity, timeSinceInvalidTarget: Optional<Long>): Boolean {
        return timeSinceInvalidTarget.isPresent && entity.level().gameTime - timeSinceInvalidTarget.get() > 200L
    }
}