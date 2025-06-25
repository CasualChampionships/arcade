/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai.behavior

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.utils.isWithinAttackRange
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import kotlin.jvm.optionals.getOrNull

public object FakePlayerSetWalkTargetFromAttackTarget {
    public fun create(speedModifier: Float): BehaviorControl<FakePlayer> {
        return this.create { speedModifier }
    }

    public fun <E: FakePlayer> create(speedModifier: (E) -> Float): BehaviorControl<E> {
        return BehaviorBuilder.create { instance ->
            instance.group(
                instance.registered(MemoryModuleType.WALK_TARGET),
                instance.registered(MemoryModuleType.LOOK_TARGET),
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.registered(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            ).apply(instance) { walkTargetAccessor, lookTargetAccessor, attackTargetAccessor, nearestEntitiesAccessor ->
                Trigger(fun(_, player, _): Boolean {
                    val target = instance.get(attackTargetAccessor)
                    val entities = instance.tryGet(nearestEntitiesAccessor).getOrNull()
                    if (entities != null && entities.contains(target) && player.isWithinAttackRange(target, 1)) {
                        walkTargetAccessor.erase()
                    } else {
                        lookTargetAccessor.set(EntityTracker(target, true))
                        val speed = speedModifier.invoke(player)
                        val walkTarget = WalkTarget(EntityTracker(target, false), speed, 0)
                        walkTargetAccessor.set(walkTarget)
                    }
                    return false
                })
            }
        }
    }
}