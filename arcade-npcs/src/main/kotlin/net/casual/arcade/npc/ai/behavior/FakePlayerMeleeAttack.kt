/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai.behavior

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.utils.PlayerUtils.getAttackCooldown
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.item.ProjectileWeaponItem

public object FakePlayerMeleeAttack {
    public fun <E: FakePlayer> create(
        canAttack: (E) -> Boolean = { true },
        attackCooldown: (E) -> MinecraftTimeDuration = { it.getAttackCooldown() }
    ): OneShot<E> {
        return BehaviorBuilder.create { instance ->
            instance.group(
                instance.registered(MemoryModuleType.LOOK_TARGET),
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN),
                instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            ).apply(instance) { lookTargetAccessor, attackTargetAccessor, cooldownAccessor, nearestEntitiesAccessor ->
                Trigger(fun(_, player, _): Boolean {
                    val target = instance.get(attackTargetAccessor)
                    if (canAttack.invoke(player) && canMeleeAttack(player, target)) {
                        val entities = instance.get(nearestEntitiesAccessor)
                        if (entities.contains(target)) {
                            lookTargetAccessor.set(EntityTracker(target, true))
                            player.swing(InteractionHand.MAIN_HAND)
                            player.attack(target)
                            val cooldown = attackCooldown.invoke(player).ticks.toLong()
                            cooldownAccessor.setWithExpiry(true, cooldown)
                            return true
                        }
                    }

                    return false
                })
            }
        }
    }

    private fun canMeleeAttack(player: FakePlayer, target: LivingEntity): Boolean {
        return !isHoldingUsableProjectileWeapon(player) && player.isWithinMeleeAttackRange(target)
    }

    private fun isHoldingUsableProjectileWeapon(player: FakePlayer): Boolean {
        return player.isHolding { stack ->
            val item = stack.item
            item is ProjectileWeaponItem && player.canFireProjectileWeapon(item)
        }
    }
}