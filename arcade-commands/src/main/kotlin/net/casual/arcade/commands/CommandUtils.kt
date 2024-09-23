package net.casual.arcade.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.commands.hidden.HiddenCommandManager
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.utils.ComponentUtils.command
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.function.Supplier

@Suppress("UnusedReceiverParameter")
public fun Any?.commandSuccess(): Int {
    return 1
}

@Suppress("UnusedReceiverParameter")
public fun Any?.commandFailure(): Int {
    return 0
}

public fun MutableComponent.singleUseFunction(command: HiddenCommand): MutableComponent {
    return this.function { context ->
        command.run(context)
        context.remove()
    }
}

public fun MutableComponent.function(timeout: MinecraftTimeDuration = 10.Minutes, command: HiddenCommand): MutableComponent {
    return this.command(HiddenCommandManager.register(timeout, command))
}

public fun CommandSourceStack.success(literal: String, log: Boolean = false): Int {
    return this.success(Component.literal(literal), log)
}

public fun CommandSourceStack.success(component: Component, log: Boolean = false): Int {
    return this.sendSuccess({ component }, log).commandSuccess()
}

public fun CommandSourceStack.success(log: Boolean = false, generator: Supplier<Component>): Int {
    return this.sendSuccess(generator, log).commandSuccess()
}

public fun CommandSourceStack.fail(literal: String): Int {
    return this.fail(Component.literal(literal))
}

public fun CommandSourceStack.fail(component: Component): Int {
    return this.sendFailure(component).commandFailure()
}

public inline fun <S> CommandDispatcher<S>.registerLiteral(
    literal: String,
    builder: LiteralArgumentBuilder<S>.() -> Unit
): LiteralCommandNode<S> {
    val root = LiteralArgumentBuilder.literal<S>(literal)
    root.builder()
    return this.register(root)
}

public inline fun <S, T: ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.literal(
    literal: String,
    builder: LiteralArgumentBuilder<S>.() -> Unit = { }
): LiteralArgumentBuilder<S> {
    val argument = LiteralArgumentBuilder.literal<S>(literal)
    argument.builder()
    this.then(argument)
    return argument
}

public inline fun <A, S, T: ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.argument(
    name: String,
    type: ArgumentType<A>,
    builder: RequiredArgumentBuilder<S, A>.() -> Unit = { }
): RequiredArgumentBuilder<S, A> {
    val first = RequiredArgumentBuilder.argument<S, A>(name, type)
    first.builder()
    this.then(first)
    return first
}

public inline fun <E, S, T: ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.option(
    options: Iterable<E>,
    stringifier: (E) -> String = { it.toString() },
    builder: LiteralArgumentBuilder<S>.(E) -> Unit = { }
): ArgumentBuilder<S, T> {
    for (option in options) {
        val name = stringifier.invoke(option)
        val literal = LiteralArgumentBuilder.literal<S>(name)
        literal.builder(option)
        this.then(literal)
    }
    return this
}

public inline fun <reified E: Enum<E>, S, T: ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.option(
    stringifier: (E) -> String = { it.name.lowercase() },
    builder: LiteralArgumentBuilder<S>.(E) -> Unit = { }
): ArgumentBuilder<S, T> {
    return this.option(enumValues<E>().toList(), stringifier, builder)
}

public fun <T: ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requiresPermission(
    permission: Int
): T {
    return this.requires { it.hasPermission(permission) }
}

public fun <S, T> RequiredArgumentBuilder<S, T>.suggests(
    suggestions: Iterable<String>
): RequiredArgumentBuilder<S, T> {
    return this.suggests { _, builder ->
        SharedSuggestionProvider.suggest(suggestions, builder)
    }
}

public fun <S, T> RequiredArgumentBuilder<S, T>.suggests(
    supplier: (CommandContext<S>) -> Iterable<String>
): RequiredArgumentBuilder<S, T> {
    return this.suggests { context, builder ->
        SharedSuggestionProvider.suggest(supplier(context), builder)
    }
}

public fun ServerRegisterCommandEvent.register(vararg commands: CommandTree) {
    for (command in commands) {
        command.register(this.dispatcher, this.context)
    }
}