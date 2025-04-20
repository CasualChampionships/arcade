/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.navigation

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.pathfinding.NPCPathfinder
import net.casual.arcade.npc.pathfinding.evaluator.NPCAmphibiousNodeEvaluator
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

public class NPCAmphibiousPathNavigation(player: FakePlayer) : NPCPathNavigation(player) {
    override fun createPathfinder(maxVisitedNodes: Int): NPCPathfinder {
        this.nodeEvaluator = NPCAmphibiousNodeEvaluator()
        return NPCPathfinder(this.nodeEvaluator, maxVisitedNodes)
    }

    override fun canUpdatePath(): Boolean {
        return true
    }

    override fun getTempMobPos(): Vec3 {
        return Vec3(this.player.x, this.player.getY(0.5), this.player.z)
    }

    override fun getGroundY(vec: Vec3): Double {
        return vec.y
    }

    override fun canMoveDirectly(start: Vec3, end: Vec3): Boolean {
        return this.player.isInLiquid && this.isClearForMovementBetween(this.player, start, end, false)
    }

    override fun isStableDestination(pos: BlockPos): Boolean {
        return !this.level.getBlockState(pos.below()).isAir
    }

    override fun setCanFloat(canSwim: Boolean) {

    }
}