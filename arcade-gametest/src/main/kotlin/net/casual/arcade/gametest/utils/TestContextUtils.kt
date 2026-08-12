/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.utils

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.Location
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

public suspend fun TestContext.createTestPlayer(x: Int, y: Int, z: Int, yaw: Float = 0.0F, pitch: Float = 0.0F): TestFakePlayer {
    return this.createTestPlayer(x + 0.5, y.toDouble(), z + 0.5, yaw, pitch)
}

public suspend fun TestContext.createTestPlayer(x: Double, y: Double, z: Double, yaw: Float = 0.0F, pitch: Float = 0.0F): TestFakePlayer {
    val player = this.createTestPlayer()
    player.teleportTo(Location(x, y, z, yaw, pitch))
    return player
}

public fun TestContext.absolute(x: Int, y: Int, z: Int): BlockPos {
    return this.absolute(BlockPos(x, y, z))
}

public fun TestContext.absolute(pos: BlockPos): BlockPos {
    return this.helper.absolutePos(pos)
}

public fun TestContext.absolute(x: Double, y: Double, z: Double): Vec3 {
    return this.absolute(Vec3(x, y, z))
}

public fun TestContext.absolute(pos: Vec3): Vec3 {
    return this.helper.absoluteVec(pos)
}

public fun TestContext.setBlock(x: Int, y: Int, z: Int, block: Block) {
    this.helper.setBlock(x, y, z, block)
}

public fun TestContext.setBlock(x: Int, y: Int, z: Int, state: BlockState) {
    this.helper.setBlock(x, y, z, state)
}

public fun TestContext.fill(fromX: Int, fromY: Int, fromZ: Int, toX: Int, toY: Int, toZ: Int, block: Block) {
    this.fill(BlockPos(fromX, fromY, fromZ), BlockPos(toX, toY, toZ), block)
}

public fun TestContext.fill(from: BlockPos, to: BlockPos, block: Block) {
    for (pos in BlockPos.betweenClosed(from, to)) {
        this.helper.setBlock(pos, block)
    }
}
