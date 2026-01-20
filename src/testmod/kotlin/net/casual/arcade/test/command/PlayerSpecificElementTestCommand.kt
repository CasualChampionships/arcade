package net.casual.arcade.test.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.executes
import net.casual.arcade.visuals.entity.element.PlayerSpecificTextDisplayElement
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.Display

object PlayerSpecificElementTestCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("player-specific-element-test") {
            executes(::test)
        }
    }

    private fun test(context: CommandContext<CommandSourceStack>) {
        val holder = ElementHolder()
        val element = PlayerSpecificTextDisplayElement { observer -> observer.displayName!! }
        element.setBillboardConstraints(Display.BillboardConstraints.CENTER)
        holder.addElement(element)

        ChunkAttachment.ofTicking(holder, context.source.level, context.source.position)
    }
}