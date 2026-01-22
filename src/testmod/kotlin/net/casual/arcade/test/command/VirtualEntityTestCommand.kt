package net.casual.arcade.test.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.utils.MathUtils.rotationAnglesTowards
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.coroutine.launch
import net.casual.arcade.virtual.entity.SimpleVirtualEntity
import net.casual.arcade.virtual.entity.attachment.SimpleVirtualEntityAttachment
import net.casual.arcade.virtual.entity.location.VirtualPosition
import net.casual.arcade.virtual.entity.location.VirtualRotation
import net.casual.arcade.virtual.entity.utils.attach
import net.casual.arcade.virtual.entity.utils.createVirtualEntityAttachment
import net.casual.arcade.virtual.entity.utils.removeVirtualEntityAttachment
import net.casual.arcade.visuals.shapes.impl.RegularPolygonShape
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.EntityType

object VirtualEntityTestCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("virtual-entity-test") {
            literal("level-attachment") {
                executes(::testLevelAttachment)
            }
        }
    }

    private fun testLevelAttachment(context: CommandContext<CommandSourceStack>) {
        val level = context.source.level
        val position = context.source.position
        val shape = RegularPolygonShape(position, 5.0, 10)

        val attachment = level.createVirtualEntityAttachment(::SimpleVirtualEntityAttachment)
        for (point in shape) {
            val zombie = attachment.attach { SimpleVirtualEntity(EntityType.ZOMBIE, it) }
            zombie.position = VirtualPosition.Absolute(point)
            zombie.rotation = VirtualRotation.Absolute(point.rotationAnglesTowards(position))
        }
        context.source.server.launch {
            delay(10.Seconds)
            level.removeVirtualEntityAttachment(attachment)
        }
    }
}