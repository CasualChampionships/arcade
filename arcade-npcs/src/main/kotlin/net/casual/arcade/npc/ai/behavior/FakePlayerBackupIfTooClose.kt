/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai.behavior

import net.casual.arcade.npc.FakePlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

public object FakePlayerBackupIfTooClose {
    public fun <T: FakePlayer> create(tooCloseDistance: (T) -> Double): OneShot<T> {
        return BehaviorBuilder.create { instance ->
            instance.group(
                instance.absent(MemoryModuleType.WALK_TARGET),
                instance.registered(MemoryModuleType.LOOK_TARGET),
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            ).apply(instance) { _, lookTargetAccessor, attackTargetAccessor, nearestEntitiesAccessor ->
                Trigger(fun(_, player, _): Boolean {
                    val target = instance.get(attackTargetAccessor)
                    val distance = tooCloseDistance.invoke(player)
                    if (target.closerThan(player, distance) && instance.get(nearestEntitiesAccessor).contains(target)) {
                        lookTargetAccessor.set(EntityTracker(target, true))
                        player.moveControl.strafe(-1.0F, 0.0f)
                        player.yRot = Mth.rotateIfNecessary(player.yRot, player.yHeadRot, 0.0f)
                        return true
                    }
                    return false
                })
            }
        }
    }
}