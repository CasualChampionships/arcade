package net.casual.arcade.utils

import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.commands.hidden.HiddenCommandContext
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCommandEvent
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import org.jetbrains.annotations.ApiStatus.Experimental

object CommandUtils {
    private val commands = HashMap<String, HiddenCommand>()
    private val random = RandomSource.create()

    private const val ROOT = "~arcade\$hidden\$command"

    @Experimental
    @JvmStatic
    fun registerHiddenCommand(command: HiddenCommand): String {
        val name = "$ROOT ${Mth.createInsecureUUID(this.random)}"
        this.commands[name] = command
        return "/$name"
    }

    @JvmStatic
    @Suppress("UnusedReceiverParameter")
    fun Any?.commandSuccess(): Int {
        return 1
    }

    @JvmStatic
    @Suppress("UnusedReceiverParameter")
    fun Any?.commandFailure(): Int {
        return 0
    }

    fun CommandSourceStack.success(component: Component, log: Boolean = false): Int {
        return this.sendSuccess({ component }, log).commandSuccess()
    }

    fun CommandSourceStack.fail(component: Component): Int {
        return this.sendFailure(component).commandFailure()
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCommandEvent> { event ->
            val (player, string) = event
            val command = this.commands[string]
            if (command != null && command.canRun(player)) {
                val context = HiddenCommandContext(player)
                command.run(context)
                if (context.remove) {
                    this.commands.remove(string)
                }
                event.cancel()
            }
        }
    }
}