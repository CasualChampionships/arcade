/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.firework

import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.asLocation
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.projectile.FireworkRocketEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.FireworkExplosion
import net.minecraft.world.item.component.Fireworks
import net.minecraft.world.phys.Vec3

public class VirtualFireworkBuilder(level: ServerLevel) {
    private val explosions = ArrayList<FireworkExplosion>()

    public var location: LocationWithLevel<ServerLevel> = level.asLocation()
    public var angled: Boolean = false
    public var duration: MinecraftTimeDuration = 10.Ticks
    public var velocity: Vec3? = null

    public fun location(location: LocationWithLevel<ServerLevel>): VirtualFireworkBuilder {
        this.location = location
        return this
    }

    public fun angled(): VirtualFireworkBuilder {
        this.angled = true
        return this
    }

    public fun duration(duration: MinecraftTimeDuration): VirtualFireworkBuilder {
        this.duration = duration
        return this
    }

    public fun velocity(velocity: Vec3): VirtualFireworkBuilder {
        this.velocity = velocity
        return this
    }

    public fun explosion(block: FireworkExplosionBuilder.() -> Unit): VirtualFireworkBuilder {
        val builder = FireworkExplosionBuilder()
        builder.block()
        this.explosions.add(builder.build())
        return this
    }

    public fun build(): VirtualFirework {
        val stack = ItemStack(Items.FIREWORK_ROCKET)
        stack.set(DataComponents.FIREWORKS, Fireworks(0, this.explosions))
        val entity = FireworkRocketEntity(this.location.level, stack, this.location.x, this.location.y, this.location.z, this.angled)
        val velocity = this.velocity
        if (velocity != null) {
            entity.deltaMovement = velocity
        }
        return VirtualFirework(entity, this.duration)
    }
}