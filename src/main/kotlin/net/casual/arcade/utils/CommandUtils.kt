package net.casual.arcade.utils

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.commands.hidden.HiddenCommandContext
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCommandEvent
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.TimeUtils.Minutes
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import org.jetbrains.annotations.ApiStatus.Experimental
import java.util.function.Supplier

public object CommandUtils {
    private val removed = HashMap<String, (ServerPlayer) -> Component>()
    private val commands = HashMap<String, HiddenCommand>()
    private val random = RandomSource.create()

    private const val ROOT = "~arcade\$hidden\$command"

    @Experimental
    @JvmStatic
    public fun registerHiddenCommand(timeout: MinecraftTimeDuration = 10.Minutes, command: HiddenCommand): String {
        val name = "$ROOT ${Mth.createInsecureUUID(this.random)}"
        this.commands[name] = command
        GlobalTickedScheduler.schedule(timeout) {
            this.commands.remove(name)
        }
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

    public fun CommandSourceStack.success(log: Boolean = false, generator: Supplier<Component>): Int {
        return this.sendSuccess(generator, log).commandSuccess()
    }

    public fun CommandSourceStack.fail(literal: String): Int {
        return this.fail(literal.literal())
    }

    public fun CommandSourceStack.fail(component: Component): Int {
        return this.sendFailure(component).commandFailure()
    }

    public fun <S> buildLiteral(
        literal: String,
        builder: LiteralArgumentBuilder<S>.() -> Unit
    ): LiteralCommandNode<S> {
        val root = LiteralArgumentBuilder.literal<S>(literal)
        root.builder()
        return root.build()
    }

    public fun <S> CommandDispatcher<S>.buildLiteral(
        literal: String,
        builder: LiteralArgumentBuilder<S>.() -> Unit
    ): LiteralCommandNode<S> {
        val root = LiteralArgumentBuilder.literal<S>(literal)
        root.builder()
        return this.register(root)
    }

    public fun <S, T: ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.literal(
        literal: String,
        builder: LiteralArgumentBuilder<S>.() -> Unit = { }
    ): T {
        val argument = LiteralArgumentBuilder.literal<S>(literal)
        argument.builder()
        return this.then(argument)
    }

    public fun <S, T: ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.literal(
        root: String,
        vararg literals: String,
        builder: LiteralArgumentBuilder<S>.() -> Unit = { }
    ): T {
        val first = LiteralArgumentBuilder.literal<S>(root)
        var argument = first
        for (literal in literals) {
            argument = argument.literal(literal)
        }
        argument.builder()
        return this.then(first)
    }

    public fun <A, S, T: ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.argument(
        name: String,
        type: ArgumentType<A>,
        builder: RequiredArgumentBuilder<S, A>.() -> Unit = { }
    ): T {
        val argument = RequiredArgumentBuilder.argument<S, A>(name, type)
        argument.builder()
        return this.then(argument)
    }

    public fun <T: ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requiresPermission(
        permission: Int
    ): T {
        return this.requires { it.hasPermission(permission) }
    }

    public fun <S, T: RequiredArgumentBuilder<S, T>> RequiredArgumentBuilder<S, T>.suggests(
        suggestions: Iterable<String>
    ): RequiredArgumentBuilder<S, T> {
        return this.suggests { _, builder ->
            SharedSuggestionProvider.suggest(suggestions, builder)
        }
    }

    public fun <S, T: RequiredArgumentBuilder<S, T>> RequiredArgumentBuilder<S, T>.suggests(
        supplier: (CommandContext<S>) -> Iterable<String>
    ): RequiredArgumentBuilder<S, T> {
        return this.suggests { context, builder ->
            SharedSuggestionProvider.suggest(supplier(context), builder)
        }
    }

    public fun RequiredArgumentBuilder<CommandSourceStack, ResourceLocation>.suggests(
        supplier: (CommandContext<CommandSourceStack>) -> Iterable<String>
    ): RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> {
        return this.suggests { context, builder ->
            SharedSuggestionProvider.suggest(supplier(context), builder)
        }
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