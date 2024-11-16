package net.casual.arcade.minigame.managers

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.CommandNode
import net.casual.arcade.commands.ducks.DeletableCommand
import net.casual.arcade.commands.fail
import net.casual.arcade.events.player.PlayerCommandEvent
import net.casual.arcade.events.player.PlayerCommandSuggestionsEvent
import net.casual.arcade.events.player.PlayerSendCommandsEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.*
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.ComponentUtils.command
import net.casual.arcade.utils.ComponentUtils.grey
import net.casual.arcade.utils.ComponentUtils.hover
import net.casual.arcade.utils.ComponentUtils.italicise
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ComponentUtils.underline
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.server.level.ServerPlayer
import java.util.*

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
    private val registered = LinkedList<String>()

    init {
        this.minigame.events.register<PlayerSendCommandsEvent> {
            it.addCustomCommandNode(this.dispatcher.root)
        }
        this.minigame.events.register<PlayerCommandEvent> {
            this.onCommand(it)
        }
        this.minigame.events.register<PlayerCommandSuggestionsEvent> {
            this.onCommandSuggestions(it)
        }
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
        this.registered.add(literal.literal)
        this.dispatcher.register(literal)

        val global = this.getGlobalMinigameCommand() ?: return
        global.addChild(
            Commands.literal(this.minigame.uuid.toString()).then(literal).build()
        )
        this.resendGlobalCommands()
    }

    /**
     * This method unregisters a command from the minigame.
     *
     * @param name The name of the command to unregister.
     */
    public fun unregister(name: String) {
        if (this.registered.remove(name)) {
            (this.dispatcher as DeletableCommand).`arcade$delete`(name)
            val command = this.getGlobalMinigameCommand()?.getChild(this.minigame.uuid.toString()) ?: return
            (command as DeletableCommand).`arcade$delete`(name)
            this.resendGlobalCommands()
        }
    }

    /**
     * This method unregisters all commands from the minigame.
     */
    public fun unregisterAll() {
        val dispatcher = this.dispatcher as DeletableCommand
        for (name in this.registered) {
            dispatcher.`arcade$delete`(name)
            val command = this.getGlobalMinigameCommand()?.getChild(this.minigame.uuid.toString()) ?: return
            (command as DeletableCommand).`arcade$delete`(name)
        }
        this.registered.clear()
        this.resendGlobalCommands()
    }

    /**
     * This method gets all the root commands registered to the minigame.
     *
     * @return The collection of root commands.
     */
    public fun getAllRootCommands(): Collection<String> {
        return this.registered
    }

    public fun resendCommands() {
        for (player in this.minigame.players) {
            this.resendCommandsTo(player)
        }
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
        // FIXME:
        // return this.minigame.server.commands.dispatcher.root.getChild("minigame")?.getChild("command")
    }

    private fun onCommand(event: PlayerCommandEvent) {
        val source = event.player.createCommandSourceStack()
        val result = this.dispatcher.parse(event.command, source)
        if (!result.reader.canRead()) {
            source.server.commands.performCommand(result, event.command)
            event.cancel()
        }
    }

    private fun onCommandSuggestions(event: PlayerCommandSuggestionsEvent) {
        val result = this.dispatcher.parse(event.createCommandReader(), event.player.createCommandSourceStack())
        event.addSuggestions(this.dispatcher.getCompletionSuggestions(result))
    }
}