/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.boundary.extension.LevelBoundaryExtension
import net.casual.arcade.boundary.renderer.BoundaryRenderer
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.boundary.shape.BoundaryShape.Containment
import net.casual.arcade.boundary.utils.ClientboundSetBorderWarningDistancePacket
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.core.TrackedPlayerUI
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.Consumer
import kotlin.math.max

/**
 * This represents a boundary for a given [ServerLevel].
 *
 * It serves a similar purpose to [WorldBorder] but allows
 * for much more customization.
 *
 * @param shape The shape of the boundary.
 * @param renderer The renderer to render this boundary.
 * @see LevelBoundaryExtension.levelBoundary
 * @see BoundaryShape
 * @see BoundaryRenderer
 */
public class LevelBoundary(
    /**
     * The shape of the boundary.
     */
    public val shape: BoundaryShape,
    /**
     * The boundary renderer.
     */
    public val renderer: BoundaryRenderer,
): TrackedPlayerUI() {
    /**
     * Determines how much damage to deal to the player
     * while they're outside the boundary.
     */
    public var damagePerBlock: Double = 0.2

    /**
     * The number of blocks the player won't take
     * damage outside the border.
     */
    public var damageSafeZone: Double = 5.0

    /**
     * The distance from the boundary at which to
     * start displaying a vignette.
     */
    public var warningBlocks: Int = 5

    /**
     * Constructs a boundary from the given [Settings].
     *
     * @param settings The boundary settings.
     */
    public constructor(settings: Settings): this(settings.shape, settings.renderer()) {
        this.damagePerBlock = settings.damagePerBlock
        this.damageSafeZone = settings.damageSafeZone
        this.warningBlocks = settings.warningBlocks
    }

    /**
     * Resizes the boundary to the specified [size] over
     * a [duration].
     *
     * A duration of [MinecraftTimeDuration.ZERO] makes
     * the resize instant.
     *
     * @param size The desired size.
     * @param duration The duration to change the size over.
     */
    public fun resize(size: Vec3, duration: MinecraftTimeDuration = MinecraftTimeDuration.ZERO) {
        this.shape.resize(size, duration)
    }

    /**
     * Re-centers the boundary to the specified [center] over
     * a [duration].
     *
     * A duration of [MinecraftTimeDuration.ZERO] makes
     * the re-centering instant.
     *
     * @param center The desired center.
     * @param duration The duration to change the center over.
     */
    public fun recenter(center: Vec3, duration: MinecraftTimeDuration = MinecraftTimeDuration.ZERO) {
        this.shape.recenter(center, duration)
    }

    /**
     * Whether a given [BlockPos] is contained in the border.
     *
     * This treats the position as a unit cube,
     * see [contains] for implementation details.
     *
     * @param pos The pos to check.
     * @return The [Containment] of the position.
     */
    public fun contains(pos: BlockPos): Containment {
        return this.shape.contains(pos)
    }

    /**
     * Checks whether a [point] is within the border.
     *
     * @param point The point to check.
     * @return Whether the point is in the border.
     */
    public fun contains(point: Vec3): Boolean {
        return this.shape.contains(point)
    }

    /**
     * Gets the distance to a given [point].
     *
     * @param point The point to get the distance to.
     * @return The distance.
     */
    public fun getDistanceBetween(point: Vec3): Double {
        return this.shape.getDistanceBetween(point)
    }

    /**
     * Gets the direction (with magnitude) from a given [point].
     *
     * @param point The point to get the direction to.
     */
    public fun getDirectionFrom(point: Vec3): Vec3 {
        return this.shape.getDirectionFrom(point)
    }

    /**
     * This gets the approximate size of the boundary,
     * i.e. the x-length, y-length, and z-length.
     *
     * @return The size of the boundary.
     */
    public fun getSize(): Vec3 {
        return this.shape.size()
    }

    /**
     * This gets the center of the boundary.
     *
     * @return The center of the boundary.
     */
    public fun getCenter(): Vec3 {
        return this.shape.center()
    }

    /**
     * Gets an approximate bounding box for the boundary.
     *
     * The boundary shape may *not* be an AABB, this is just
     * another way to describe the center and size of the boundary.
     *
     * @return The approximate AABB.
     */
    public fun getAABB(): AABB {
        val center = this.getCenter()
        val size = this.getSize()
        return AABB.ofSize(center, size.x, size.y, size.z)
    }

    /**
     * Gets the status of the border, whether
     * it's currently stationary, shrinking, or growing.
     *
     * @return The border status.
     */
    public fun getStatus(): BoundaryShape.Status {
        return this.shape.getStatus()
    }

    /**
     * Creates the settings for this [LevelBoundary].
     *
     * @return The settings.
     */
    public fun createSettings(): Settings {
        return Settings(
            this.shape, this.renderer.factory(), this.damagePerBlock, this.damageSafeZone, this.warningBlocks
        )
    }

    /**
     * Updates the boundary shape and renderer.
     * This is also responsible for dealing damage
     * to players outside the boundary.
     *
     * @param level The level this boundary is for.
     */
    public fun tick(level: ServerLevel) {
        this.shape.tick()

        val players = this.getPlayers()
        this.renderer.render(level, players)
        for (player in players) {
            this.tickPlayer(player)
        }
    }

    override fun onAddPlayer(player: ServerPlayer) {
        this.renderer.startRendering(player)
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        this.renderer.stopRendering(player)
        player.connection.send(INSIDE_BORDER_PACKET)
    }

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        this.renderer.restartRendering(player, sender)
    }

    private fun tickPlayer(player: ServerPlayer) {
        val position = player.position()
        if (!player.isAlive) {
            return
        }

        val distance = this.getDistanceBetween(position)
        val inside = this.contains(position)
        if (inside) {
            if (distance < this.warningBlocks) {
                val ratio = (this.warningBlocks / distance)
                val simulated = player.level().worldBorder.getDistanceToBorder(player) * ratio
                player.connection.send(ClientboundSetBorderWarningDistancePacket(simulated.toInt()))
            } else {
                player.connection.send(INSIDE_BORDER_PACKET)
            }
            return
        }

        player.connection.send(OUTSIDE_BORDER_PACKET)
        val damagingDistance = distance - this.damageSafeZone
        if (damagingDistance > 0 && this.damagePerBlock > 0) {
            val damage = max(1, Mth.floor(damagingDistance * this.damagePerBlock)).toFloat()
            player.hurtServer(player.level(), player.damageSources().outOfBorder(), damage)
        }
    }

    /**
     * Utility class representing a size and center.
     */
    public data class SizeAndCenter(val size: Vec3, val center: Vec3) {
        public fun aabb(): AABB {
            return AABB.ofSize(this.center, this.size.x, this.size.y, this.size.z)
        }
    }

    public data class Settings(
        val shape: BoundaryShape,
        val rendererFactory: BoundaryRenderer.Factory,
        val damagePerBlock: Double,
        val damageSafeZone: Double,
        val warningBlocks: Int
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
                    Codec.DOUBLE.fieldOf("damage_safe_zone").forGetter(Settings::damageSafeZone),
                    Codec.INT.fieldOf("warning_blocks").forGetter(Settings::warningBlocks)
                ).apply(instance, ::Settings)
            }
        }
    }

    private companion object {
        val INSIDE_BORDER_PACKET = ClientboundSetBorderWarningDistancePacket(0)
        val OUTSIDE_BORDER_PACKET = ClientboundSetBorderWarningDistancePacket(Int.MAX_VALUE)
    }
}