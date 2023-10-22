package net.casual.arcade.utils

import com.mojang.brigadier.Command
import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.commands.hidden.HiddenCommandContext
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCommandEvent
import net.casual.arcade.utils.ComponentUtils.literal
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import org.jetbrains.annotations.ApiStatus.Experimental

public object CommandUtils {
    private val removed = HashMap<String, (ServerPlayer) -> Component>()
    private val commands = HashMap<String, HiddenCommand>()
    private val random = RandomSource.create()

    private const val ROOT = "~arcade\$hidden\$command"

    @Experimental
    @JvmStatic
    public fun registerHiddenCommand(command: HiddenCommand): String {
        val name = "$ROOT ${Mth.createInsecureUUID(this.random)}"
        this.commands[name] = command
        return "/$name"
    }

    @JvmStatic
    @Suppress("UnusedReceiverParameter")
    public fun Any?.commandSuccess(): Int {
        return Command.SINGLE_SUCCESS
    }

    @JvmStatic
    @Suppress("UnusedReceiverParameter")
    public fun Any?.commandFailure(): Int {
        return 0
    }

    public fun CommandSourceStack.success(literal: String, log: Boolean = false): Int {
        return this.success(literal.literal(), log)
    }

    public fun CommandSourceStack.success(component: Component, log: Boolean = false): Int {
        return this.sendSuccess({ component }, log).commandSuccess()
    }

    public fun CommandSourceStack.fail(literal: String): Int {
        return this.fail(literal.literal())
    }

    public fun CommandSourceStack.fail(component: Component): Int {
        return this.sendFailure(component).commandFailure()
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCommandEvent> { event ->
            val (player, string) = event
            val removedMessage = this.removed[string]
            if (removedMessage != null) {
                player.sendSystemMessage(removedMessage(player))
                event.cancel()
            } else {
                val command = this.commands[string]
                if (command != null && command.canRun(player)) {
                    val context = HiddenCommandContext(player)
                    command.run(context)
                    val message = context.removedMessage
                    if (message != null) {
                        this.commands.remove(string)
                        this.removed[string] = message
                    }
                    event.cancel()
                }
            }
        }
    }
}