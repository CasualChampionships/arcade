package net.casual.arcade.entity.firework

import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.location.Location
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.projectile.FireworkRocketEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.FireworkExplosion
import net.minecraft.world.item.component.Fireworks

public class VirtualFireworkBuilder {
    private val explosions = ArrayList<FireworkExplosion>()

    public var location: Location = Location.of()
    public var angled: Boolean = false
    public var duration: MinecraftTimeDuration = 10.Ticks

    public fun location(location: Location): VirtualFireworkBuilder {
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
        return VirtualFirework(entity, this.duration)
    }
}