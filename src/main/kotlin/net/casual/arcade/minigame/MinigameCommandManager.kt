package net.casual.arcade.minigame

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.CommandNode
import net.casual.arcade.utils.ducks.DeletableCommand
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import java.util.*

public class MinigameCommandManager(
    private val minigame: Minigame<*>,
) {
    private val registered = LinkedList<String>()

    public fun register(literal: LiteralArgumentBuilder<CommandSourceStack>) {
        val dispatcher = this.minigame.server.commands.dispatcher

        this.registered.add(literal.literal)
        literal.requires(literal.requirement.and { this.minigame.hasPlayer(it.playerOrException) })
        dispatcher.register(literal)


        val global = this.getGlobalMinigameCommand() ?: return
        global.addChild(
            Commands.literal(this.minigame.uuid.toString()).then(literal).build()
        )
        this.resendCommands()
    }

    public fun unregister(name: String) {
        if (this.registered.remove(name)) {
            (this.minigame.server.commands.dispatcher as DeletableCommand).delete(name)
            val command = this.getGlobalMinigameCommand()?.getChild(this.minigame.uuid.toString()) ?: return
            (command as DeletableCommand).delete(name)
            this.resendCommands()
        }
    }

    public fun unregisterAll() {
        val deletableRegister = this.minigame.server.commands.dispatcher as DeletableCommand
        for (name in this.registered) {
            deletableRegister.delete(name)
            val command = this.getGlobalMinigameCommand()?.getChild(this.minigame.uuid.toString()) ?: return
            (command as DeletableCommand).delete(name)
        }
        this.registered.clear()
        this.resendCommands()
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