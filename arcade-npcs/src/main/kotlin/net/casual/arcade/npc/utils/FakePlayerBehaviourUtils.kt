/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.utils

import net.casual.arcade.npc.FakePlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ProjectileWeaponItem
import kotlin.math.acos

public fun FakePlayer.isFacing(target: Entity, tolerance: Float = 15.0F): Boolean {
    val eye = if (target is LivingEntity) target.eyePosition else target.position()
    val delta = eye.subtract(this.eyePosition)
    if (delta.lengthSqr() < Mth.EPSILON.toDouble()) {
        return true
    }
    val angle = Math.toDegrees(acos(delta.normalize().dot(this.lookAngle).coerceIn(-1.0, 1.0)))
    return angle <= tolerance
}

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