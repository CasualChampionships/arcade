/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.ducks.DeletableCommand
import net.casual.arcade.commands.literal
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.block.CommandBlockExecuteEvent
import net.casual.arcade.events.server.player.PlayerCommandEvent
import net.casual.arcade.events.server.player.PlayerCommandSuggestionsEvent
import net.casual.arcade.events.server.player.PlayerSendCommandsEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.*
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer

/**
 * This class manages the commands of a minigame.
 *
 * All commands added to this manager are local to the
 * minigame only and do not exist outside the context
 * of the minigame.
 *
 * @see Minigame.commands
 */
public class MinigameCommandManager(
    private val minigame: Minigame,
) {
    private val dispatcher = CommandDispatcher<CommandSourceStack>()

    init {
        this.minigame.events.register<PlayerSendCommandsEvent> {
            it.addCustomCommandNode(this.dispatcher.root)
        }
        this.minigame.events.register<PlayerCommandEvent>(this::onPlayerCommand)
        this.minigame.events.register<CommandBlockExecuteEvent>(this::onBlockCommand)
        this.minigame.events.register<PlayerCommandSuggestionsEvent>(this::onCommandSuggestions)
        this.minigame.events.register<MinigameCloseEvent> {
            this.unregisterAll()
        }

        // Resending the command tree...
        this.minigame.events.register<MinigameAddPlayerEvent> {
            this.resendCommandsTo(it.player)
        }
        this.minigame.events.register<MinigameRemovePlayerEvent> {
            this.resendCommandsTo(it.player)
        }
        this.minigame.events.register<MinigameAddAdminEvent> {
            this.resendCommandsTo(it.player)
        }
        this.minigame.events.register<MinigameRemoveAdminEvent> {
            this.resendCommandsTo(it.player)
        }
    }

    /**
     * This method registers a command to the minigame.
     *
     * @param literal The command builder to register.
     */
    public fun register(literal: LiteralArgumentBuilder<CommandSourceStack>) {
        this.dispatcher.register(literal)
        this.registerLiterals(listOf(literal.literal))
    }

    /**
     * This method registers a command tree to the minigame.
     *
     * @param tree The command tree to register.
     *
     * @see CommandTree
     */
    public fun register(tree: CommandTree) {
        val before = this.getAllRootCommands().toSet()

        val server = this.minigame.server
        val context = CommandBuildContext.simple(server.registryAccess(), server.worldData.enabledFeatures())
        tree.register(this.dispatcher, context)

        val after = this.getAllRootCommands().toMutableSet()
        after.removeAll(before)
        if (after.isNotEmpty()) {
            this.registerLiterals(after)
        }
    }

    /**
     * This method unregisters a command from the minigame.
     *
     * @param name The name of the command to unregister.
     */
    public fun unregister(name: String) {
        if ((this.dispatcher as DeletableCommand).`arcade$delete`(name)) {
            val command = this.getGlobalMinigameCommand()?.getChild(this.minigame.uuid.toString()) ?: return
            (command as DeletableCommand).`arcade$delete`(name)
            this.resendGlobalCommands()
        }
    }

    /**
     * This method unregisters all commands from the minigame.
     */
    public fun unregisterAll() {
        val roots = this.getAllRootCommands()
        if (roots.isEmpty()) {
            return
        }

        val dispatcher = this.dispatcher as DeletableCommand
        val global = this.getGlobalMinigameCommand()?.getChild(this.minigame.uuid.toString()) as? DeletableCommand
        for (name in roots) {
            dispatcher.`arcade$delete`(name)
            global?.`arcade$delete`(name)
        }

        this.resendGlobalCommands()
    }

    /**
     * This method gets all the root commands registered to the minigame.
     *
     * @return The collection of root commands.
     */
    public fun getAllRootCommands(): Collection<String> {
        return this.dispatcher.root.children
            .filterIsInstance<LiteralCommandNode<*>>()
            .map { it.literal }
    }

    public fun resendCommands() {
        for (player in this.minigame.players) {
            this.resendCommandsTo(player)
        }
    }

    private fun registerLiterals(literals: Collection<String>) {
        val global = this.getGlobalMinigameCommand() ?: return

        val node = CommandTree.createLiteral<CommandSourceStack>(this.minigame.uuid.toString()) {
            for (literal in literals) {
                literal(literal)
            }
        }
        global.addChild(node)
        this.resendGlobalCommands()
    }

    private fun resendGlobalCommands() {
        for (player in this.minigame.server.playerList.players) {
            this.resendCommandsTo(player)
        }
    }

    private fun resendCommandsTo(player: ServerPlayer) {
        this.minigame.server.commands.sendCommands(player)
    }

    private fun getGlobalMinigameCommand(): CommandNode<CommandSourceStack>? {
        return null
    }

    private fun onPlayerCommand(event: PlayerCommandEvent) {
        val source = event.player.createCommandSourceStack()
        val result = this.dispatcher.parse(event.command, source)
        if (!result.reader.canRead()) {
            source.server.commands.performCommand(result, event.command)
            event.cancel()
        }
    }

    private fun onBlockCommand(event: CommandBlockExecuteEvent) {
        val result = this.dispatcher.parse(event.command, event.source)
        if (!result.reader.canRead()) {
            event.source.server.commands.performCommand(result, event.command)
            event.cancel()
        }
    }

    private fun onCommandSuggestions(event: PlayerCommandSuggestionsEvent) {
        val result = this.dispatcher.parse(event.createCommandReader(), event.player.createCommandSourceStack())
        event.addSuggestions(this.dispatcher.getCompletionSuggestions(result))
    }
}