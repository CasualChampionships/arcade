/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ai.behavior

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.utils.isWithinAttackRange
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.behavior.BehaviorUtils
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.Items
import net.minecraft.world.item.ProjectileWeaponItem

public class FakePlayerBowAttack(
    minDuration: MinecraftTimeDuration,
    maxDuration: MinecraftTimeDuration
): Behavior<FakePlayer>(CONDITIONS, minDuration.ticks, maxDuration.ticks) {
    override fun checkExtraStartConditions(level: ServerLevel, player: FakePlayer): Boolean {
        val target = player.brain.getMemory(MemoryModuleType.ATTACK_TARGET).get()
        return player.isHolding(Items.BOW) && BehaviorUtils.canSee(player, target)
            && player.isWithinAttackRange(target, 0.0)
    }

    override fun canStillUse(level: ServerLevel, player: FakePlayer, gameTime: Long): Boolean {
        return player.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
            && this.checkExtraStartConditions(level, player)
    }

    override fun tick(level: ServerLevel, player: FakePlayer, gameTime: Long) {
        val target = player.brain.getMemory(MemoryModuleType.ATTACK_TARGET).get()
        player.brain.setMemory(MemoryModuleType.LOOK_TARGET, EntityTracker(target, true))

        val hand = ProjectileUtil.getWeaponHoldingHand(player, Items.BOW)
        val item = player.getItemInHand(hand)
        if (player.isUsingItem) {
            val using = player.ticksUsingItem
            if (using >= BowItem.MAX_DRAW_DURATION) {
                player.releaseUsingItem()
            }
        } else {
            player.gameMode.useItem(player, level, item, hand)
        }
    }

    override fun stop(level: ServerLevel, player: FakePlayer, gameTime: Long) {
        if (player.isUsingItem) {
            player.stopUsingItem()
        }
    }

    public companion object {
        private val CONDITIONS = mapOf(
            MemoryModuleType.LOOK_TARGET to MemoryStatus.REGISTERED,
            MemoryModuleType.ATTACK_TARGET to MemoryStatus.VALUE_PRESENT
        )
    }
}