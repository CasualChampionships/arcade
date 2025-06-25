/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai.behavior

import net.casual.arcade.npc.FakePlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.StartAttacking.StartAttackingCondition
import net.minecraft.world.entity.ai.behavior.StartAttacking.TargetFinder
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

public object FakePlayerStartAttacking {
    public fun <E: FakePlayer> create(
        condition: StartAttackingCondition<E> = StartAttackingCondition { _, _ -> true },
        targetFinder: TargetFinder<E>
    ): BehaviorControl<E> {
        return BehaviorBuilder.create { instance ->
            instance.group(
                instance.absent(MemoryModuleType.ATTACK_TARGET),
                instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
            ).apply(instance) { targetMemoryAccessor, timeSinceInvalidMemoryAccessor ->
                Trigger(fun(level, player, _): Boolean {
                    if (!condition.test(level, player)) {
                        return false
                    }
                    val optional = targetFinder.get(level, player)
                    if (optional.isPresent) {
                        val target = optional.get()
                        if (!player.canAttack(target)) {
                            return false
                        }
                        targetMemoryAccessor.set(target)
                        timeSinceInvalidMemoryAccessor.erase()
                        return true
                    }
                    return false
                })
            }
        }
    }
}