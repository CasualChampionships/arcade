/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.utils

import com.mojang.authlib.GameProfile
import kotlinx.coroutines.future.await
import net.casual.arcade.gametest.TestContext
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.configuration.FakePlayerConstructor
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.Location
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

public class TestPlayerBuilder<T: TestFakePlayer> internal constructor(
    private val context: TestContext,
    private val constructor: FakePlayerConstructor<T>
) {
    private var name: String? = null
    private var position: Vec3 = Vec3(0.5, 0.0, 0.5)
    private var yaw: Float = 0.0F
    private var pitch: Float = 0.0F
    private var gameMode: GameType = GameType.SURVIVAL
    private var recordLoginPackets: Boolean = false

    public fun <U: TestFakePlayer> constructor(constructor: FakePlayerConstructor<U>): TestPlayerBuilder<U> {
        val builder = TestPlayerBuilder(this.context, constructor)
        builder.name = this.name
        builder.position = this.position
        builder.yaw = this.yaw
        builder.pitch = this.pitch
        builder.gameMode = this.gameMode
        builder.recordLoginPackets = this.recordLoginPackets
        return builder
    }

    public fun name(name: String): TestPlayerBuilder<T> {
        this.name = name
        return this
    }

    public fun position(x: Int, y: Int, z: Int): TestPlayerBuilder<T> {
        return this.position(Vec3(x + 0.5, y.toDouble(), z + 0.5))
    }

    public fun position(pos: BlockPos): TestPlayerBuilder<T> {
        return this.position(pos.x, pos.y, pos.z)
    }

    public fun position(x: Double, y: Double, z: Double): TestPlayerBuilder<T> {
        return this.position(Vec3(x, y, z))
    }

    public fun position(pos: Vec3): TestPlayerBuilder<T> {
        this.position = pos
        return this
    }

    public fun rotation(yaw: Float, pitch: Float = 0.0F): TestPlayerBuilder<T> {
        this.yaw = yaw
        this.pitch = pitch
        return this
    }

    public fun gameMode(mode: GameType): TestPlayerBuilder<T> {
        this.gameMode = mode
        return this
    }


    public fun recordLoginPackets(): TestPlayerBuilder<T> {
        this.recordLoginPackets = true
        return this
    }


    public suspend fun spawn(): T {
        val name = this.name ?: TestContext.nextTestPlayerName()
        val profile = GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name)
        val player = FakePlayer.join(this.context.server, profile, this.constructor).await()
        player.context = this.context
        player.setGameMode(this.gameMode)
        this.context.track(player)

        if (!this.recordLoginPackets) {
            player.clearPackets()
        }
        player.teleportTo(Location(this.context.absolute(this.position), Vec2(this.pitch, this.yaw)))
        return player
    }
}
