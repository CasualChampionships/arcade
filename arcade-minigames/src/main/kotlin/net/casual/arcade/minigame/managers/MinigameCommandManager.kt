/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import net.minecraft.world.level.storage.ValueOutput
import com.mojang.serialization.Codec
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.ducks.DeletableCommand
import net.casual.arcade.commands.literal
import net.casual.arcade.commands.manager.ServerCommandManager
import net.casual.arcade.events.server.block.CommandBlockExecuteEvent
import net.casual.arcade.events.server.player.PlayerCommandEvent
import net.casual.arcade.events.server.player.PlayerCommandSuggestionsEvent
import net.casual.arcade.events.server.player.PlayerSendCommandsEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.*
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
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
): ServerCommandManager(minigame.server) {
    init {
        this.initialize(this.minigame.events)

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
     * This method unregisters a command from the minigame.
     *
     * @param name The name of the command to unregister.
     */
    public fun unregister(name: String) {
        (this.dispatcher as DeletableCommand).arcade_deleteCommand(name)
        this.resendCommands()
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
        for (name in roots) {
            dispatcher.arcade_deleteCommand(name)
        }

        this.resendCommands()
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

    public override fun resendCommands() {
        for (player in this.minigame.players) {
            this.resendCommandsTo(player)
        }
    }

    internal fun debug(output: ValueOutput) {
        output.store("all", Codec.STRING.listOf(), this.getAllRootCommands().toList())
    }

    private fun resendCommandsTo(player: ServerPlayer) {
        this.minigame.server.commands.sendCommands(player)
    }
}