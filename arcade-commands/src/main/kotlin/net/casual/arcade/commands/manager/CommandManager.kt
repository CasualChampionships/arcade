/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.manager

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.events.ListenerRegistry
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.block.CommandBlockExecuteEvent
import net.casual.arcade.events.server.player.PlayerCommandEvent
import net.casual.arcade.events.server.player.PlayerCommandSuggestionsEvent
import net.casual.arcade.events.server.player.PlayerSendCommandsEvent
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer

public class CommandManager(
    private val server: MinecraftServer
): CommandRegistry {
    private val dispatcher = CommandDispatcher<CommandSourceStack>()

    override fun register(literal: LiteralArgumentBuilder<CommandSourceStack>) {
        this.dispatcher.register(literal)
        this.resendCommands()
    }

    override fun register(tree: CommandTree) {
        val context = CommandBuildContext.simple(this.server.registryAccess(), this.server.worldData.enabledFeatures())
        tree.register(this.dispatcher, context)
        this.resendCommands()
    }

    internal fun initialize(registry: ListenerRegistry) {
        registry.register<PlayerSendCommandsEvent>(this::onPlayerSendCommands)
        registry.register<PlayerCommandEvent>(this::onPlayerCommand)
        registry.register<CommandBlockExecuteEvent>(this::onCommandBlockExecute)
        registry.register<PlayerCommandSuggestionsEvent>(this::onPlayerCommandSuggestions)
    }

    internal fun close() {
        this.resendCommands()
    }

    private fun resendCommands() {
        for (player in this.server.playerList.players) {
            this.server.commands.sendCommands(player)
        }
    }

    private fun onPlayerSendCommands(event: PlayerSendCommandsEvent) {
        event.addCustomCommandNode(this.dispatcher.root)
    }

    private fun onPlayerCommand(event: PlayerCommandEvent) {
        val source = event.player.createCommandSourceStack()
        val result = this.dispatcher.parse(event.command, source)
        if (!result.reader.canRead()) {
            source.server.commands.performCommand(result, event.command)
            event.cancel()
        }
    }

    private fun onCommandBlockExecute(event: CommandBlockExecuteEvent) {
        val result = this.dispatcher.parse(event.command, event.source)
        if (!result.reader.canRead()) {
            event.source.server.commands.performCommand(result, event.command)
            event.cancel()
        }
    }

    private fun onPlayerCommandSuggestions(event: PlayerCommandSuggestionsEvent) {
        val result = this.dispatcher.parse(event.createCommandReader(), event.player.createCommandSourceStack())
        event.addSuggestions(this.dispatcher.getCompletionSuggestions(result))
    }
}