/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.manager

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.dispatcher
import net.casual.arcade.commands.parseResults
import net.casual.arcade.events.ListenerRegistry
import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.server.block.CommandBlockExecuteEvent
import net.casual.arcade.events.server.player.PlayerCommandEvent
import net.casual.arcade.events.server.player.PlayerCommandSuggestionsEvent
import net.casual.arcade.events.server.player.PlayerSendCommandsEvent
import net.casual.arcade.events.utils.register
import net.minecraft.commands.*
import net.minecraft.server.MinecraftServer

public typealias CommandManager = ServerCommandManager

public open class ServerCommandManager(
    protected val server: MinecraftServer
): CommandRegistry<CommandSourceStack> {
    protected val dispatcher: CommandDispatcher<CommandSourceStack> = CommandDispatcher()
    protected var parseBox: ParseBox = ParseBox.fromFullTree(this.dispatcher)
    protected var suggestionBox: SuggestionBox = SuggestionBox.fromFullTree(this.dispatcher)

    override fun register(literal: LiteralArgumentBuilder<CommandSourceStack>) {
        this.dispatcher.register(literal)
        this.resendCommands()

        this.parseBox = ParseBox.fromFullTree(this.dispatcher)
        this.suggestionBox = SuggestionBox.fromFullTree(this.dispatcher)
    }

    override fun register(tree: CommandTree<CommandSourceStack>) {
        val context = CommandBuildContext.simple(this.server.worldData.enabledFeatures())
        tree.register(this.dispatcher, context)
        this.resendCommands()

        this.parseBox = ParseBox.fromFullTree(this.dispatcher)
        this.suggestionBox = SuggestionBox.fromFullTree(this.dispatcher)
    }

    internal fun initialize(registry: ListenerRegistry<ServerSideEvent>) {
        registry.register<PlayerSendCommandsEvent>(this::onPlayerSendCommands)
        registry.register<PlayerCommandEvent>(this::onPlayerCommand)
        registry.register<CommandBlockExecuteEvent>(this::onCommandBlockExecute)
        registry.register<PlayerCommandSuggestionsEvent>(this::onPlayerCommandSuggestions)
    }

    internal fun close() {
        this.resendCommands()
    }

    protected fun resendCommands() {
        for (player in this.server.playerList.players) {
            this.server.commands.sendCommands(player)
        }
    }

    private fun onPlayerSendCommands(event: PlayerSendCommandsEvent) {
        event.addCustomCommandNode(this.suggestionBox.dispatcher.root)
    }

    private fun onPlayerCommand(event: PlayerCommandEvent) {
        val source = event.player.createCommandSourceStack()
        val result = this.parseBox.parse(event.command, ParseContext.of(source))
        if (!result.parseResults.reader.canRead()) {
            Commands.performCommand(source, result, event.command)
            event.cancel()
        }
    }

    private fun onCommandBlockExecute(event: CommandBlockExecuteEvent) {
        val result = this.parseBox.parse(event.command, ParseContext.of(event.source))
        if (!result.parseResults.reader.canRead()) {
            Commands.performCommand(event.source, result, event.command)
            event.cancel()
        }
    }

    private fun onPlayerCommandSuggestions(event: PlayerCommandSuggestionsEvent) {
        event.addSuggestions(this.suggestionBox)
    }
}