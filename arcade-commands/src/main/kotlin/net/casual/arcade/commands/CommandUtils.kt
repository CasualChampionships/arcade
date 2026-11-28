/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import net.casual.arcade.commands.mixins.CommandBoxAccessor
import net.casual.arcade.commands.mixins.ParseBoxResultAccessor
import net.casual.arcade.commands.requirements.PlayerCommandRequirement
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.math.location.asLocation
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates
import net.minecraft.commands.CommandBox
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.ParseBox
import net.minecraft.commands.ParseContext
import net.minecraft.commands.ServerPlayerSuggestionContext
import net.minecraft.commands.suggestions.SuggestionContext
import net.minecraft.commands.suggestions.SuggestionProviders
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.Permission.HasCommandLevel
import net.minecraft.server.permissions.PermissionCheck
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.server.permissions.PermissionSetSupplier
import java.util.function.Supplier

public val CommandSourceStack.location: Location
    get() = Location(this.position, this.rotation)

public val CommandSourceStack.locationWithLevel: LocationWithLevel<ServerLevel>
    get() = this.level.asLocation(this.position, this.rotation)

public val ServerPlayerSuggestionContext.player: ServerPlayer
    get() = this.entity() as ServerPlayer

public fun CommandSourceStack.success(literal: String, log: Boolean = false): Int {
    return this.success(Component.literal(literal), log)
}

public fun CommandSourceStack.success(component: Component, log: Boolean = false): Int {
    this.sendSuccess({ component }, log)
    return Command.SINGLE_SUCCESS
}

public fun CommandSourceStack.success(log: Boolean = false, generator: Supplier<Component>): Int {
    this.sendSuccess(generator, log)
    return Command.SINGLE_SUCCESS
}

public fun CommandSourceStack.fail(literal: String): Int {
    return this.fail(Component.literal(literal))
}

public fun CommandSourceStack.fail(component: Component): Int {
    this.sendFailure(component)
    return 0
}

public fun CommandSourceStack.hasPermission(permission: PermissionLevel): Boolean {
    return this.hasPermission(HasCommandLevel(permission))
}

public fun CommandSourceStack.hasPermission(permission: Permission): Boolean {
    return this.permissions().hasPermission(permission)
}

public inline fun <S> CommandDispatcher<S>.registerLiteral(
    literal: String,
    builder: LiteralArgumentBuilder<S>.() -> Unit
): LiteralCommandNode<S> {
    val root = LiteralArgumentBuilder.literal<S>(literal)
    root.builder()
    return this.register(root)
}

public inline fun <S, T: ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.executes(
    crossinline command: (CommandContext<S>) -> Unit
): ArgumentBuilder<S, T> {
    this.executes { context ->
        command.invoke(context)
        Command.SINGLE_SUCCESS
    }
    return this
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
    permission: PermissionLevel
): T {
    return this.requires(Commands.hasPermission(PermissionCheck.Require(HasCommandLevel(permission))))
}

public fun <T: ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requiresPermission(
    permission: Permission
): T {
    return this.requires(Commands.hasPermission(PermissionCheck.Require(permission)))
}

public fun <T: ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requiresPermission(
    permission: Identifier
): T {
    return this.requires(PermissionPredicates.require(permission))
}

public fun <T: ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requiresPermission(
    permission: Identifier,
    fallback: PermissionLevel
): T {
    return this.requires(PermissionPredicates.require(permission, fallback))
}

public fun <T: ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requiresPermission(
    permission: Identifier,
    fallback: Boolean
): T {
    return this.requires(PermissionPredicates.require(permission, fallback))
}

public fun <T: ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requiresPlayer(
    fallback: Boolean = false,
    predicate: (ServerPlayer) -> Boolean,
): T {
    return this.requiresPlayerOr(predicate) { fallback }
}

public fun <T: ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requiresPlayerOr(
    predicate: (ServerPlayer) -> Boolean,
    fallback: (PermissionSetSupplier) -> Boolean
): T {
    return this.requires(PlayerCommandRequirement(predicate, fallback))
}

public fun <T> RequiredArgumentBuilder<CommandSourceStack, T>.suggests(
    suggestions: Iterable<String>
): RequiredArgumentBuilder<CommandSourceStack, T> {
    return this.suggests(SuggestionProviders.serverOnly { _, _, builder ->
        SuggestionContext.suggest(suggestions, builder)
    })
}

public fun <T> RequiredArgumentBuilder<CommandSourceStack, T>.suggests(
    supplier: (CommandContext<out SuggestionContext>) -> Iterable<String>
): RequiredArgumentBuilder<CommandSourceStack, T> {
    return this.suggests(SuggestionProviders.serverOnly { _, context, builder ->
        SuggestionContext.suggest(supplier.invoke(context), builder)
    })
}

public fun CommandContext<*>.hasArgument(argument: String): Boolean {
    return this.hasArgument(argument, Any::class.java)
}

public fun <T> CommandContext<*>.hasArgument(argument: String, type: Class<T>): Boolean {
    try {
        this.getArgument(argument, type)
        return true
    } catch (_: IllegalArgumentException) {
        return false
    }
}

public inline fun <T, S> CommandContext<S>.getArgumentOrElse(
    argument: String,
    getter: (CommandContext<S>, String) -> T,
    otherwise: () -> T
): T {
    return try {
        getter.invoke(this, argument)
    } catch (_: IllegalArgumentException) {
        otherwise.invoke()
    }
}

public inline fun <T: Any, S> CommandContext<S>.getArgumentOrNull(
    argument: String,
    getter: (CommandContext<S>, String) -> T
): T? {
    return this.getArgumentOrElse(argument, getter) { null }
}

public fun ServerRegisterCommandEvent.register(vararg commands: CommandTree<CommandSourceStack>) {
    for (command in commands) {
        command.register(this.dispatcher, this.context)
    }
}

public fun ParseBox.parseWithPlayer(
    player: ServerPlayer,
    command: String,
    context: ParseContext = ParseContext.of(player)
): ParseBox.Result {
    return ScopedValue.where(PlayerCommandRequirement.PLAYER_CONTEXT, player)
        .call<_, Throwable> { this.parse(command, context) }
}

internal val ParseBox.Result.parseResults: ParseResults<ParseContext>
    get() = (this as ParseBoxResultAccessor).arcade_getParseResults()

@Suppress("UNCHECKED_CAST")
internal val <C: ParseContext> CommandBox<C>.dispatcher: CommandDispatcher<C>
    get() = (this as CommandBoxAccessor<C>).arcade_getDispatcher()