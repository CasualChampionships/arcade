package net.casual.arcade.utils

import net.casual.arcade.gui.shapes.ArcadeShape
import net.casual.arcade.utils.PlayerUtils.sendParticles
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer

public object ShapeUtils {
    public fun ArcadeShape.drawAsParticlesFor(
        player: ServerPlayer,
        particle: ParticleOptions = ParticleTypes.END_ROD,
        steps: Int = 10
    ) {
        for (point in this.iterator(steps)) {
            player.sendParticles(particle, point)
        }
    }
}