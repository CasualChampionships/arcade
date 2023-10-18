package net.casual.arcade.minigame.managers

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.CommandNode
import net.casual.arcade.events.minigame.MinigameCloseEvent
import net.casual.arcade.events.player.PlayerCommandEvent
import net.casual.arcade.events.player.PlayerSendCommandsEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.ducks.DeletableCommand
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import java.util.*

public class MinigameCommandManager(
    private val minigame: Minigame<*>,
) {
    private val dispatcher = CommandDispatcher<CommandSourceStack>()
    private val registered = LinkedList<String>()

    init {
        this.minigame.events.register<PlayerSendCommandsEvent> {
            it.addCustomCommandNode(this.dispatcher.root)
        }
        this.minigame.events.register<PlayerCommandEvent> {
            val result = this.dispatcher.parse(it.command, it.player.createCommandSourceStack())
            if (!result.reader.canRead()) {
                this.dispatcher.execute(result)
                it.cancel()
            }
        }
        this.minigame.events.register<MinigameCloseEvent> {
            this.unregisterAll()
        }
    }

    public fun register(literal: LiteralArgumentBuilder<CommandSourceStack>) {
        this.registered.add(literal.literal)
        this.dispatcher.register(literal)

        val global = this.getGlobalMinigameCommand() ?: return
        global.addChild(
            Commands.literal(this.minigame.uuid.toString()).then(literal).build()
        )
        this.resendCommands()
    }

    public fun unregister(name: String) {
        if (this.registered.remove(name)) {
            (this.dispatcher as DeletableCommand).delete(name)
            val command = this.getGlobalMinigameCommand()?.getChild(this.minigame.uuid.toString()) ?: return
            (command as DeletableCommand).delete(name)
            this.resendCommands()
        }
    }

    public fun unregisterAll() {
        val dispatcher = this.dispatcher as DeletableCommand
        for (name in this.registered) {
            dispatcher.delete(name)
            val command = this.getGlobalMinigameCommand()?.getChild(this.minigame.uuid.toString()) ?: return
            (command as DeletableCommand).delete(name)
        }
        this.registered.clear()
        this.resendCommands()
    }

    public fun getAllRootCommands(): Collection<String> {
        return this.registered
    }

    private fun resendCommands() {
        for (player in this.minigame.getPlayers()) {
            this.minigame.server.commands.sendCommands(player)
        }
    }

    private fun getGlobalMinigameCommand(): CommandNode<CommandSourceStack>? {
        return this.minigame.server.commands.dispatcher.root.getChild("minigame")?.getChild("command")
    }
}