/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.utils

import net.casual.arcade.npc.FakePlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ProjectileWeaponItem

public fun FakePlayer.isWithinAttackRange(
    target: LivingEntity,
    rangedModifier: Double = -1.0,
    meleeModifier: Double = 0.0
): Boolean {
    val item = this.mainHandItem.item
    if (item is ProjectileWeaponItem && this.canFireProjectileWeapon(item)) {
        val range = item.defaultProjectileRange + rangedModifier
        return this.closerThan(target, range)
    }
    return this.isWithinMeleeAttackRange(target, meleeModifier)
}