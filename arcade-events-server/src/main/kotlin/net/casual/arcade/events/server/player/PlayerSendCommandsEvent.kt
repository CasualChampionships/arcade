/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import com.mojang.brigadier.tree.RootCommandNode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.suggestions.SuggestionContext
import net.minecraft.server.level.ServerPlayer
import java.util.*

public class PlayerSendCommandsEvent(
    override val player: ServerPlayer
): PlayerEvent {
    private val nodes = LinkedList<RootCommandNode<SuggestionContext>>()

    public fun addCustomCommandNode(root: RootCommandNode<SuggestionContext>) {
        this.nodes.add(root)
    }

    public fun getCustomCommandNodes(): List<RootCommandNode<SuggestionContext>> {
        return this.nodes
    }
}