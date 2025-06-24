/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.utils

import net.casual.arcade.npc.FakePlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ProjectileWeaponItem

public fun FakePlayer.isWithinAttackRange(target: LivingEntity, cooldown: Int): Boolean {
    val item = this.mainHandItem.item
    if (item is ProjectileWeaponItem && this.canFireProjectileWeapon(item)) {
        val range = item.defaultProjectileRange - cooldown
        return this.closerThan(target, range.toDouble())
    }
    return this.isWithinMeleeAttackRange(target)
}