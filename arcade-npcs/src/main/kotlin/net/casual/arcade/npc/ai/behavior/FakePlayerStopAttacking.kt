/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai.behavior

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid.TargetErasedCallback
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import java.util.*

public object FakePlayerStopAttacking {
    public fun <E: FakePlayer> create(
        canStopAttacking: (player: E, target: LivingEntity) -> Boolean = { _, _ -> false },
        onStopAttacking: TargetErasedCallback<E> = TargetErasedCallback { _, _, _ -> },
        tireDuration: MinecraftTimeDuration? = 200.Ticks
    ): BehaviorControl<E> {
        return BehaviorBuilder.create { instance: BehaviorBuilder.Instance<E> ->
            instance.group(
                instance.present(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
            ).apply(instance) { targetMemoryAccessor, timeSinceInvalidMemoryAccessor ->
                Trigger { level, player, _ ->
                    val target = instance.get(targetMemoryAccessor)
                    val canContinueAttacking = player.canAttack(target)
                        && (tireDuration == null || !isTired(player, instance.tryGet(timeSinceInvalidMemoryAccessor), tireDuration))
                        && target.isAlive
                        && target.level() === player.level()
                        && !canStopAttacking.invoke(player, target)
                    if (!canContinueAttacking) {
                        onStopAttacking.accept(level, player, target)
                        targetMemoryAccessor.erase()
                    }
                    true
                }
            }
        }
    }

    private fun isTired(
        entity: LivingEntity,
        timeSinceInvalidTarget: Optional<Long>,
        tireDuration: MinecraftTimeDuration
    ): Boolean {
        return timeSinceInvalidTarget.isPresent && entity.level().gameTime - timeSinceInvalidTarget.get() > tireDuration.ticks
    }
}