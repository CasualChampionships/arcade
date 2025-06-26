/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.border.renderer.BoundaryRenderer
import net.casual.arcade.border.shape.BoundaryShape
import net.casual.arcade.visuals.core.TrackedPlayerUI
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import java.util.function.Consumer
import kotlin.math.max

public class LevelBoundary(
    public val shape: BoundaryShape,
    public val renderer: BoundaryRenderer,
): TrackedPlayerUI() {
    public var damagePerBlock: Double = 0.2
    public var damageSafeZone: Double = 5.0

    public constructor(settings: Settings): this(settings.shape, settings.renderer()) {
        this.damagePerBlock = settings.damagePerBlock
        this.damageSafeZone = settings.damageSafeZone
    }

    public fun contains(pos: BlockPos): BoundaryShape.Containment {
        return this.shape.contains(pos)
    }

    public fun contains(point: Vec3): Boolean {
        return this.shape.contains(point)
    }

    public fun getDistanceTo(point: Vec3): Double {
        return this.shape.getDistanceTo(point)
    }

    public fun getDirectionTo(point: Vec3): Vec3 {
        return this.shape.getDirectionTo(point)
    }

    public fun createSettings(): Settings {
        return Settings(this.shape, this.renderer.factory(), this.damagePerBlock, this.damageSafeZone)
    }

    public fun tick() {
        this.shape.tick()

        val players = this.getPlayers()
        this.renderer.render(players)
        for (player in players) {
            this.tickPlayer(player)
        }
    }

    override fun onAddPlayer(player: ServerPlayer) {
        this.renderer.startRendering(player)
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        this.renderer.stopRendering(player)
    }

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        this.renderer.restartRendering(player, sender)
    }

    private fun tickPlayer(player: ServerPlayer) {
        val position = player.position()
        if (this.contains(position) || !player.isAlive) {
            return
        }

        val distance = this.shape.getDistanceTo(position) - this.damageSafeZone
        if (distance > 0 && this.damagePerBlock > 0) {
            val damage = max(1, Mth.floor(distance * this.damagePerBlock)).toFloat()
            player.hurtServer(player.level(), player.damageSources().outOfBorder(), damage)
        }
    }

    public data class Settings(
        val shape: BoundaryShape,
        val rendererFactory: BoundaryRenderer.Factory,
        val damagePerBlock: Double,
        val damageSafeZone: Double
    ) {
        public fun renderer(): BoundaryRenderer {
            return this.rendererFactory.create(this.shape)
        }

        public companion object {
            public val CODEC: Codec<Settings> = RecordCodecBuilder.create { instance ->
                instance.group(
                    BoundaryShape.CODEC.fieldOf("shape").forGetter(Settings::shape),
                    BoundaryRenderer.Factory.CODEC.fieldOf("renderer").forGetter(Settings::rendererFactory),
                    Codec.DOUBLE.fieldOf("damage_per_block").forGetter(Settings::damagePerBlock),
                    Codec.DOUBLE.fieldOf("damage_safe_zone").forGetter(Settings::damageSafeZone)
                ).apply(instance, ::Settings)
            }
        }
    }
}