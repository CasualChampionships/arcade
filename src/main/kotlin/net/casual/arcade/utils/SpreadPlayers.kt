package net.casual.arcade.utils

import net.casual.arcade.utils.BlockUtils.isOf
import net.minecraft.core.BlockPos
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.phys.Vec2
import net.minecraft.world.scores.Team
import kotlin.math.sqrt

internal object SpreadPlayers {
    fun run(
        level: ServerLevel,
        center: Vec2,
        distance: Double,
        range: Double,
        height: Int,
        teams: Boolean,
        targets: Collection<Entity>
    ) {
        val random = RandomSource.create()
        val minX = center.x - range
        val minZ = center.y - range
        val maxX = center.x + range
        val maxZ = center.y + range
        val positions = Array(if (teams) this.countTeams(targets) else targets.size) {
            Position().randomize(random, minX, minZ, maxX, maxZ)
        }
        spreadPositions(distance, level, random, minX, minZ, maxX, maxZ, height, positions)
        setPlayerPositions(targets, level, positions, height, teams)
    }

    private fun countTeams(entities: Collection<Entity>): Int {
        val set = HashSet<Team?>()
        for (entity in entities) {
            if (entity is Player) {
                set.add(entity.getTeam())
            } else {
                set.add(null)
            }
        }
        return set.size
    }

    private fun spreadPositions(
        distance: Double,
        level: ServerLevel,
        random: RandomSource,
        minX: Double,
        minZ: Double,
        maxX: Double,
        maxZ: Double,
        height: Int,
        positions: Array<Position>
    ) {
        var shouldContinue = true
        var i = 0
        while (i < 10000 && shouldContinue) {
            shouldContinue = false
            for (j in positions.indices) {
                val position = positions[j]
                var k = 0
                val position2 = Position()
                for (l in positions.indices) {
                    if (j != l) {
                        val position3 = positions[l]
                        val e = position.dist(position3)
                        if (e < distance) {
                            ++k
                            position2.x += position3.x - position.x
                            position2.z += position3.z - position.z
                        }
                    }
                }
                if (k > 0) {
                    position2.x /= k.toDouble()
                    position2.z /= k.toDouble()
                    val f = position2.length
                    if (f > 0.0) {
                        position2.normalize()
                        position.moveAway(position2)
                    } else {
                        position.randomize(random, minX, minZ, maxX, maxZ)
                    }
                    shouldContinue = true
                }
                if (position.clamp(minX, minZ, maxX, maxZ)) {
                    shouldContinue = true
                }
            }
            if (!shouldContinue) {
                for (position2 in positions) {
                    if (!position2.isSafe(level, height)) {
                        position2.randomize(random, minX, minZ, maxX, maxZ)
                        shouldContinue = true
                    }
                }
            }
            ++i
        }
    }

    private fun setPlayerPositions(
        targets: Collection<Entity>,
        level: ServerLevel,
        positions: Array<Position>,
        maxHeight: Int,
        respectTeams: Boolean
    ) {
        var i = 0
        val map = HashMap<Team?, Position>()
        for (entity in targets) {
            val position = if (respectTeams) {
                val team = (entity as? Player)?.team
                map.getOrPut(team) { positions[i++] }
            } else {
                positions[i++]
            }
            entity.teleportTo(
                level,
                Mth.floor(position.x) + 0.5,
                position.getSpawnY(level, maxHeight).toDouble(),
                Mth.floor(position.z) + 0.5,
                setOf(),
                entity.yRot,
                entity.xRot
            )
        }
    }

    private class Position {
        val length: Double
            get() = sqrt(x * x + z * z)

        var x = 0.0
        var z = 0.0

        fun dist(other: Position): Double {
            val d = this.x - other.x
            val e = this.z - other.z
            return sqrt(d * d + e * e)
        }

        fun normalize() {
            val d = length
            this.x /= d
            this.z /= d
        }

        fun moveAway(other: Position) {
            this.x -= other.x
            this.z -= other.z
        }

        fun clamp(minX: Double, minZ: Double, maxX: Double, maxZ: Double): Boolean {
            var changed = false
            if (this.x < minX) {
                this.x = minX
                changed = true
            } else if (this.x > maxX) {
                this.x = maxX
                changed = true
            }
            if (this.z < minZ) {
                this.z = minZ
                changed = true
            } else if (this.z > maxZ) {
                this.z = maxZ
                changed = true
            }
            return changed
        }

        fun getSpawnY(level: BlockGetter, y: Int): Int {
            val pos = MutableBlockPos(Mth.floor(this.x), y + 1, Mth.floor(this.z))
            var headAir = level.getBlockState(pos).isAir
            var feetAir = level.getBlockState(pos.move(Direction.DOWN)).isAir
            var onAir: Boolean
            while (pos.y > level.minBuildHeight) {
                onAir = level.getBlockState(pos.move(Direction.DOWN)).isAir
                if (!onAir && feetAir && headAir) {
                    return pos.y + 1
                }
                headAir = feetAir
                feetAir = onAir
            }
            return y + 1
        }

        fun isSafe(level: BlockGetter, y: Int): Boolean {
            val blockPos = BlockPos.containing(x, (getSpawnY(level, y) - 1).toDouble(), z)
            val blockState = level.getBlockState(blockPos)
            @Suppress("DEPRECATION")
            return blockPos.y < y && !blockState.liquid() && !blockState.isOf(BlockTags.FIRE)
        }

        fun randomize(random: RandomSource, minX: Double, minZ: Double, maxX: Double, maxZ: Double): Position {
            this.x = Mth.nextDouble(random, minX, maxX)
            this.z = Mth.nextDouble(random, minZ, maxZ)
            return this
        }
    }
}