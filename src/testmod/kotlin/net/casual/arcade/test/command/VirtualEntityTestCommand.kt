package net.casual.arcade.test.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.utils.MathUtils.rotationAnglesTowards
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.coroutine.launch
import net.casual.arcade.virtual.entity.SimpleParentVirtualEntity
import net.casual.arcade.virtual.entity.SimpleVirtualEntity
import net.casual.arcade.virtual.entity.attachment.SimpleVirtualEntityAttachment
import net.casual.arcade.virtual.entity.location.VirtualPosition
import net.casual.arcade.virtual.entity.location.VirtualRotation
import net.casual.arcade.virtual.entity.utils.attach
import net.casual.arcade.virtual.entity.utils.attachWithParentObservers
import net.casual.arcade.virtual.entity.utils.createVirtualEntityAttachment
import net.casual.arcade.virtual.entity.utils.removeVirtualEntityAttachment
import net.casual.arcade.visuals.shapes.impl.RegularPolygonShape
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3

object VirtualEntityTestCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("virtual-entity-test") {
            literal("level-attachment") {
                executes(::testLevelAttachment)
            }
            literal("parent-ve") {
                executes(::testParentEntity)
            }
        }
    }

    private fun testLevelAttachment(context: CommandContext<CommandSourceStack>) {
        val level = context.source.level
        val position = context.source.position
        val shape = RegularPolygonShape(position, 5.0, 10)
        val attachment = level.createVirtualEntityAttachment(::SimpleVirtualEntityAttachment)
        for (point in shape) {
            val zombie = attachment.attach { a -> SimpleVirtualEntity(EntityType.ZOMBIE, a) }
            zombie.position = VirtualPosition.Absolute(point)
            zombie.rotation = VirtualRotation.Absolute(point.rotationAnglesTowards(position))
        }
        context.source.server.launch {
            delay(10.Seconds)
            level.removeVirtualEntityAttachment(attachment)
        }
    }

    private fun testParentEntity(context: CommandContext<CommandSourceStack>) {
        val level = context.source.level
        val position = context.source.position
        val attachment = level.createVirtualEntityAttachment(::SimpleVirtualEntityAttachment)
        val root = attachment.attach(::SimpleParentVirtualEntity)
        root.position = VirtualPosition.Absolute(position)
        val shape = RegularPolygonShape(Vec3.ZERO, 3.0, 10)
        for (point in shape) {
            root.attachWithParentObservers { a, o ->
                val slime = SimpleVirtualEntity(EntityType.SLIME, a, o)
                slime.position = VirtualPosition.Relative(point)
                slime.rotation = VirtualRotation.Absolute(point.rotationAnglesTowards(Vec3.ZERO))
                slime
            }
        }
        context.source.server.launch {
            repeat(50) {
                root.position += Vec3(0.1, 0.0, 0.0)
                delay(1.Ticks)
            }
            level.removeVirtualEntityAttachment(attachment)
        }
    }
}