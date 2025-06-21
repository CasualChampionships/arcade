/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.type

import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.synchronization.SuggestionProviders
import net.minecraft.network.protocol.game.ClientboundCommandsPacket.NodeInspector
import net.minecraft.resources.ResourceLocation

public class CustomCommandNodeInspector(
    private val wrapped: NodeInspector<CommandSourceStack>
): NodeInspector<CommandSourceStack> {
    override fun suggestionId(node: ArgumentCommandNode<CommandSourceStack, *>): ResourceLocation? {
        val type = node.type
        if (type is CustomArgumentType<*>) {
            val provider = type.getSuggestionProvider()
            if (provider != null) {
                return SuggestionProviders.getName(provider)
            }
        }
        return this.wrapped.suggestionId(node)
    }

    override fun isExecutable(node: CommandNode<CommandSourceStack>): Boolean {
        return this.wrapped.isExecutable(node)
    }

    override fun isRestricted(node: CommandNode<CommandSourceStack>): Boolean {
        return this.wrapped.isRestricted(node)
    }
}