/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.npc

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.gametest.utils.TestFakePlayer
import net.casual.arcade.gametest.utils.absolute
import net.casual.arcade.gametest.utils.targetMob
import net.casual.arcade.npc.ArcadeNPCs
import net.casual.arcade.npc.ai.behavior.FakePlayerBackupIfTooClose
import net.casual.arcade.npc.ai.behavior.FakePlayerBowAttack
import net.casual.arcade.npc.ai.behavior.FakePlayerLookAtTargetSink
import net.casual.arcade.npc.ai.behavior.FakePlayerMeleeAttack
import net.casual.arcade.npc.ai.behavior.FakePlayerMoveToTargetSink
import net.casual.arcade.npc.ai.behavior.FakePlayerSetWalkTargetFromAttackTarget
import net.casual.arcade.npc.ai.behavior.FakePlayerStartAttacking
import net.casual.arcade.npc.ai.behavior.FakePlayerStopAttacking
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.with
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.StartAttacking.TargetFinder
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.entity.ai.sensing.Sensor
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.*

@Suppress("FunctionName", "Unused")
object NPCBehaviorTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeNPCs.MOD_ID

    private const val FACING_TOLERANCE = 10.0F

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `walks to its walk target`(context: TestContext) = context.test {
        val player = player(0, 1, 2).brained().spawn()
        player.initialize(core = listOf(FakePlayerMoveToTargetSink()))

        val goal = absolute(10, 1, 2)
        player.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(goal, 1.0F, 0))

        assertEventually(2.Seconds, "Never started pathing towards its walk target") {
            player.hasMemory(MemoryModuleType.PATH)
        }
        assertArrives(player, goal)
        assertEventually(2.Seconds, "Walk target was not erased once it was reached") {
            !player.hasMemory(MemoryModuleType.WALK_TARGET)
        }
    }

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `stops when walk target is close enough`(context: TestContext) = context.test {
        val player = player(0, 1, 2).brained().spawn()
        player.initialize(core = listOf(FakePlayerMoveToTargetSink()))

        val goal = absolute(10, 1, 2)
        player.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(goal, 1.0F, 4))

        assertEventually(15.Seconds, "Never got close enough to its walk target") {
            player.blockPosition().distManhattan(goal) <= 4
        }
        assertEventually(2.Seconds, "Kept walking after getting close enough to its walk target") {
            !player.hasMemory(MemoryModuleType.WALK_TARGET) && player.navigation.isDone()
        }
        assertTrue(
            player.blockPosition().distManhattan(goal) >= 2,
            "Walked all the way to a walk target it was already close enough to"
        )
    }

    @GameTest(structure = "arcade:room", maxTicks = 600)
    fun `follows a walk target that moves`(context: TestContext) = context.test {
        val player = player(0, 1, 2).brained().spawn()
        player.initialize(core = listOf(FakePlayerMoveToTargetSink()))

        val target = targetMob(6, 1, 2).spawn()
        player.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(target, 1.0F, 0))

        assertEventually(2.Seconds, "Never started pathing towards its walk target") {
            player.hasMemory(MemoryModuleType.PATH)
        }

        val original = player.memory(MemoryModuleType.PATH)
        target.teleportTo(context.absolute(10.5, 1.0, 4.5).with(target.rotationVector))

        assertEventually(5.Seconds, "Walk target moving did not produce a new path") {
            player.memory(MemoryModuleType.PATH) != original
        }
        assertArrives(player, target.blockPosition())
    }

    @GameTest(structure = "arcade:sealed", maxTicks = 200)
    fun `records when its walk target cannot be reached`(context: TestContext) = context.test {
        val player = player(0, 1, 0).brained().spawn()
        player.initialize(core = listOf(FakePlayerMoveToTargetSink()))

        val goal = absolute(8, 1, 0)
        player.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(goal, 1.0F, 0))

        assertEventually(5.Seconds, "Never recorded that it couldn't reach its walk target") {
            player.hasMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
        }
    }

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `looks at its look target`(context: TestContext) = context.test {
        val player = player(3, 1, 2).brained().spawn()
        player.initialize(core = listOf(FakePlayerLookAtTargetSink(45.Ticks, 90.Ticks)))

        val target = targetMob(5, 1, 0).spawn()
        assertSees(player, target)
        player.setMemory(MemoryModuleType.LOOK_TARGET, EntityTracker(target, true))

        assertEventually(5.Seconds, "Never turned to look at its look target") {
            Mth.degreesDifferenceAbs(player.yRot, yawTowards(player, target)) < FACING_TOLERANCE
        }
    }

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `starts attacking the nearest visible entity`(context: TestContext) = context.test {
        val player = player(0, 1, 2).brained().spawn()
        player.initialize(idle = listOf(FakePlayerStartAttacking.create(targetFinder = nearestAttackable())))

        val target = targetMob(4, 1, 2).spawn()
        assertEventually(5.Seconds, "Never started attacking a visible entity") {
            player.memory(MemoryModuleType.ATTACK_TARGET) == target
        }
    }

    @GameTest(structure = "arcade:room", maxTicks = 600)
    fun `walks towards its target`(context: TestContext) = context.test {
        val player = player(0, 1, 2).brained().spawn()
        player.initialize(
            core = listOf(FakePlayerLookAtTargetSink(45.Ticks, 90.Ticks), FakePlayerMoveToTargetSink()),
            fight = listOf(FakePlayerSetWalkTargetFromAttackTarget.create(1.0F))
        )

        val target = targetMob(10, 1, 2).spawn()
        assertSees(player, target)
        player.setMemory(MemoryModuleType.ATTACK_TARGET, target)

        assertEventually(2.Seconds, "Never set a walk target from its attack target") {
            player.hasMemory(MemoryModuleType.WALK_TARGET)
        }
        assertEventually(15.Seconds, "Never closed in on its attack target") {
            player.isWithinMeleeAttackRange(target)
        }
    }

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `stops walking once its target is in range`(context: TestContext) = context.test {
        val player = player(5, 1, 2).brained().spawn()
        player.initialize(
            core = listOf(FakePlayerLookAtTargetSink(45.Ticks, 90.Ticks), FakePlayerMoveToTargetSink()),
            fight = listOf(FakePlayerSetWalkTargetFromAttackTarget.create(1.0F))
        )

        val target = targetMob(6, 1, 2).spawn()
        assertSees(player, target)
        player.setMemory(MemoryModuleType.ATTACK_TARGET, target)

        assertNever(3.Seconds, "Walked towards an attack target that was already in range") {
            player.hasMemory(MemoryModuleType.WALK_TARGET)
        }
    }

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `backs away from a target that is too close`(context: TestContext) = context.test {
        val player = player(5, 1, 2).brained().spawn()
        player.initialize(
            core = listOf(FakePlayerLookAtTargetSink(45.Ticks, 90.Ticks)),
            fight = listOf(FakePlayerBackupIfTooClose.create { 6.0 })
        )

        val target = targetMob(7, 1, 3).spawn()
        assertSees(player, target)
        player.setMemory(MemoryModuleType.ATTACK_TARGET, target)

        val original = player.distanceTo(target)
        assertEventually(10.Seconds, "Never backed away from a target that was too close") {
            player.distanceTo(target) > original + 2.0F
        }
    }

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `attacks a target within melee range`(context: TestContext) = context.test {
        val player = player(5, 1, 2).rotation(180.0F).brained().spawn()
        player.initialize(
            core = listOf(FakePlayerLookAtTargetSink(45.Ticks, 90.Ticks)),
            fight = listOf(FakePlayerMeleeAttack.create())
        )

        val target = targetMob(6, 1, 2).spawn()
        assertSees(player, target)
        player.setMemory(MemoryModuleType.ATTACK_TARGET, target)

        assertEventually(5.Seconds, "Never attacked a target stood next to it") {
            player.hasMemory(MemoryModuleType.ATTACK_COOLING_DOWN)
        }
        assertFacing(player, target, "Attacked a target it wasn't facing")
        delay(5.Ticks)
    }

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `doesnt melee a target out of range`(context: TestContext) = context.test {
        val player = player(0, 1, 2).brained().spawn()
        player.initialize(
            core = listOf(FakePlayerLookAtTargetSink(45.Ticks, 90.Ticks)),
            fight = listOf(FakePlayerMeleeAttack.create())
        )

        val target = targetMob(10, 1, 2).spawn()
        assertSees(player, target)
        player.setMemory(MemoryModuleType.ATTACK_TARGET, target)

        assertNever(3.Seconds, "Attacked a target on the other side of the room") {
            player.hasMemory(MemoryModuleType.ATTACK_COOLING_DOWN)
        }
    }

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `shoots at a target with a bow`(context: TestContext) = context.test {
        val player = player(0, 1, 2).rotation(180.0F).brained().spawn()
        player.initialize(
            core = listOf(FakePlayerLookAtTargetSink(45.Ticks, 90.Ticks)),
            fight = listOf(FakePlayerBowAttack(40.Ticks, 60.Ticks))
        )
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(Items.BOW))
        player.inventory.add(ItemStack(Items.ARROW, 8))

        val target = targetMob(10, 1, 2).spawn()
        assertSees(player, target)
        player.setMemory(MemoryModuleType.ATTACK_TARGET, target)

        assertEventually(5.Seconds, "Never drew its bow at its attack target") {
            player.isUsingItem
        }
        assertEventually(5.Seconds, "Never shot an arrow at its attack target") {
            context.level.getEntitiesOfClass(AbstractArrow::class.java, player.boundingBox.inflate(16.0)).isNotEmpty()
        }
        assertFacing(player, target, "Shot an arrow at a target it wasn't facing")
    }

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `stops attacking a target it can no longer attack`(context: TestContext) = context.test {
        val player = player(0, 1, 2).brained().spawn()
        player.initialize(
            fight = listOf(FakePlayerStopAttacking.create(
                canStopAttacking = { attacker, target -> !attacker.closerThan(target, 6.0) }
            ))
        )

        val target = targetMob(4, 1, 2).spawn()
        player.setMemory(MemoryModuleType.ATTACK_TARGET, target)

        delay(10.Ticks)
        assertTrue(player.hasMemory(MemoryModuleType.ATTACK_TARGET), "Forgot an attack target it could still attack")

        target.teleportTo(context.absolute(10.5, 1.0, 4.5).with(target.rotationVector))
        assertEventually(2.Seconds, "Kept an attack target it could no longer attack") {
            !player.hasMemory(MemoryModuleType.ATTACK_TARGET)
        }
    }

    @GameTest(structure = "arcade:room", maxTicks = 400)
    fun `tires of a target it cannot reach`(context: TestContext) = context.test {
        val player = player(0, 1, 2).brained().spawn()
        player.initialize(fight = listOf(FakePlayerStopAttacking.create()))

        val target = targetMob(4, 1, 2).spawn()
        player.setMemory(MemoryModuleType.ATTACK_TARGET, target)
        player.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, context.level.gameTime - 400L)

        assertEventually(2.Seconds, "Never tired of an attack target it couldn't reach") {
            !player.hasMemory(MemoryModuleType.ATTACK_TARGET)
        }
    }

    private fun nearestAttackable(): TargetFinder<BrainTestPlayer> {
        return TargetFinder { level, player ->
            val nearest = player.memory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            nearest?.findClosest { entity -> Sensor.isEntityAttackable(level, player, entity) }
                ?: Optional.empty<LivingEntity>()
        }
    }

    private suspend fun TestContext.assertSees(player: BrainTestPlayer, target: LivingEntity) {
        this.assertEventually(5.Seconds, "Never saw target") {
            player.sees(target)
        }
    }

    private fun TestContext.assertFacing(player: TestFakePlayer, target: LivingEntity, message: String) {
        val expected = yawTowards(player, target)
        val difference = Mth.abs(Mth.wrapDegrees(player.yRot - expected))
        this.assertTrue(difference <= FACING_TOLERANCE, "$message, facing ${player.yRot} instead of $expected")
    }

    private fun yawTowards(player: TestFakePlayer, target: LivingEntity): Float {
        val dx = target.x - player.x
        val dz = target.z - player.z
        return Mth.atan2(dz, dx).toFloat() * Mth.RAD_TO_DEG - 90.0F
    }
}
