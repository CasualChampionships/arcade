/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.utils

import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.world.level.storage.TagValueOutput
import net.minecraft.nbt.CompoundTag
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.*
import net.casual.arcade.commands.hasPermission
import net.casual.arcade.events.EventListener
import net.casual.arcade.events.EventListenerHandle
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.server.level.LevelEvent
import net.casual.arcade.events.server.level.LocatedLevelEvent
import net.casual.arcade.events.server.player.PlayerEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.event.ExtensionEvent
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.ListenerFilter
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.minigame.events.MinigameEvent
import net.casual.arcade.minigame.extensions.LevelMinigameExtension
import net.casual.arcade.minigame.extensions.PlayerMinigameExtension
import net.casual.arcade.minigame.settings.GameSetting
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.utils.component.gold
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.player.hasPermission
import net.casual.arcade.virtual.visuals.ready.ReadyChecker
import net.casual.arcade.virtual.visuals.ready.ReadyTracker
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.world.scores.PlayerTeam
import org.jetbrains.annotations.ApiStatus.Internal
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

public object MinigameUtils {
    private val PARSED = ConcurrentHashMap<Class<*>, List<ParsedListener>>()

    public val NO_MINIGAME_IN_CONTEXT: DynamicCommandExceptionType = DynamicCommandExceptionType {
        Component.translatable("minigame.command.context.noMinigameOfType", it)
    }

    internal val ServerPlayer.minigame
        get() = this.getExtension<PlayerMinigameExtension>()
    internal val ServerLevel.minigame
        get() = this.getExtension<LevelMinigameExtension>()

    @JvmStatic
    public fun ServerPlayer.getMinigame(): Minigame? {
        return this.minigame.getMinigame()
    }

    @JvmName("getTypedMinigame")
    public inline fun <reified T: Minigame> ServerPlayer.getMinigame(): T? {
        return this.getMinigame() as? T
    }

    @JvmStatic
    public fun ServerLevel.getMinigames(): Set<Minigame> {
        return this.minigame.getMinigames()
    }

    @JvmStatic
    public fun ServerLevel.getMinigames(pos: BlockPos): Set<Minigame> {
        return this.minigame.getMinigames(pos)
    }

    @JvmStatic
    public inline fun <reified T: Minigame> ServerLevel.getMinigame(): T? {
        return this.getMinigames().filterIsInstance<T>().singleOrNull()
    }

    @JvmStatic
    public inline fun <reified T: Minigame> ServerLevel.getMinigameOrThrow(): T {
        return this.getMinigame() ?: throw IllegalStateException(
            "Dimension ${this.dimension().identifier()} has no single ongoing minigame (type: ${T::class.java.simpleName})"
        )
    }

    @JvmStatic
    public inline fun <reified T: Minigame> CommandSourceStack.getMinigame(): T? {
        if (this.isPlayer) {
            val minigame = this.playerOrException.getMinigame() as? T
            if (minigame != null) {
                return minigame
            }
        }
        return this.level.getMinigame()
    }

    @JvmStatic
    public inline fun <reified T: Minigame> CommandSourceStack.getMinigameOrThrow(): T {
        return this.getMinigame() ?: throw NO_MINIGAME_IN_CONTEXT.create(T::class.java.simpleName)
    }

    @Internal
    @JvmStatic
    public fun ServerPlayer.isTicking(): Boolean {
        val minigame = this.getMinigame() ?: return true
        return !minigame.tickrate.isEntityFrozen(this)
    }

    @JvmStatic
    public fun ifSingular(minigames: Collection<Minigame>, predicate: Predicate<Minigame>): Boolean {
        if (minigames.size == 1) {
            return predicate.test(minigames.first())
        }
        return false
    }

    public inline fun Minigame.launch(crossinline block: suspend CoroutineScope.() -> Unit): Job {
        return this.scopes.root.asCoroutineScope().launch { block() }
    }

    public inline fun <T> Minigame.async(crossinline block: suspend CoroutineScope.() -> T): Deferred<T> {
        return this.scopes.root.asCoroutineScope().async { block() }
    }

    public fun Minigame.trackReadyPlayers(): ReadyTracker<ServerPlayer> {
        return ReadyChecker.track(this.visuals.playerReadyBroadcaster, this.players.playing)
    }

    public fun Minigame.trackReadyTeams(): ReadyTracker<PlayerTeam> {
        return ReadyChecker.track(this.visuals.teamReadyBroadcaster, this.teams.getPlayingTeams())
    }

    public suspend fun Minigame.checkReadyPlayers() {
        this.trackReadyPlayers().awaitSuccess()
    }

    public suspend fun Minigame.checkReadyTeams() {
        this.trackReadyTeams().awaitSuccess()
    }

    public fun <T: ArgumentBuilder<CommandSourceStack, T>> T.requiresAdminOrPermission(
        level: PermissionLevel = PermissionLevel.GAMEMASTERS
    ): T {
        return this.requires { source -> source.isMinigameAdminOrHasPermission(level) }
    }

    public fun CommandSourceStack.isMinigameAdminOrHasPermission(
        level: PermissionLevel = PermissionLevel.GAMEMASTERS
    ): Boolean {
        if (!this.isPlayer) {
            return this.hasPermission(level)
        }
        return this.playerOrException.isMinigameAdminOrHasPermission(level)
    }

    public fun CommandSourceStack.isPlayerAnd(predicate: Predicate<ServerPlayer>): Boolean {
        return this.isPlayer && predicate.test(this.playerOrException)
    }

    public fun ServerPlayer.isMinigameAdminOrHasPermission(
        level: PermissionLevel = PermissionLevel.GAMEMASTERS
    ): Boolean {
        val minigame = this.getMinigame()
        if (minigame != null && minigame.players.isAdmin(this)) {
            return true
        }
        return this.hasPermission(level)
    }

    public fun Minigame.addEventListener(listener: MinigameEventListener): EventListenerHandle {
        if (listener is Minigame) {
            throw IllegalArgumentException("Cannot parse Minigame as ${listener::class.java}")
        }
        return parseMinigameEvents(this, listener)
    }

    public fun Minigame.transferAdminAndSpectatorTeamsTo(next: Minigame) {
        if (this.teams.hasSpectatorTeam()) {
            next.teams.setSpectatorTeam(this.teams.getSpectatorTeam())
        }
        if (this.teams.hasAdminTeam()) {
            next.teams.setAdminTeam(this.teams.getAdminTeam())
        }
    }

    public fun MinigameSettings.broadcastChangesToAdmin() {
        for (setting in this.all()) {
            @Suppress("UNCHECKED_CAST")
            (setting as GameSetting<Any>).addListener { _, previous, value ->
                this.minigame.chat.broadcastTo(
                    Component.translatable(
                        "minigame.settings.changed",
                        Component.literal(setting.name).gold(),
                        Component.literal(previous.toString()).red(),
                        Component.literal(value.toString()).lime()
                    ),
                    this.minigame.players.admins
                )
            }
        }
    }

    internal fun Minigame.getDebugTag(): CompoundTag {
        return ArcadeUtils.scopedProblemReporter { reporter ->
            val output = TagValueOutput.createWithContext(reporter, this.server.registryAccess())
            this.debug(output)
            output.buildResult()
        }
    }

    internal fun parseMinigameEvents(minigame: Minigame, declarer: Any = minigame): EventListenerHandle {
        val parsed = PARSED.computeIfAbsent(declarer::class.java, ::parseListeners)
        val handles = ArrayList<EventListenerHandle>(parsed.size)
        for (listener in parsed) {
            handles.add(minigame.events.register(listener.type, listener.filters, listener.bind(declarer)))
        }
        return EventListenerHandle.of(handles)
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<LevelExtensionEvent> { event ->
            event.addExtension(::LevelMinigameExtension)
        }

        // This allows us to inject listener providers
        GlobalEventHandler.Server.addInjectedProvider { event, consumer ->
            if (event is ExtensionEvent) {
                return@addInjectedProvider
            }
            val minigames = ObjectOpenHashSet<Minigame>(3)
            if (event is PlayerEvent) {
                val minigame = event.player.getMinigame()
                if (minigame != null) {
                    minigames.add(minigame)
                }
            }
            if (event is LocatedLevelEvent) {
                minigames.addAll(event.level.getMinigames(event.pos))
            } else if (event is LevelEvent) {
                minigames.addAll(event.level.getMinigames())
            }
            if (event is MinigameEvent) {
                minigames.add(event.minigame)
            }
            for (minigame in minigames) {
                consumer.accept(minigame.events.getInjectedProvider())
            }
        }
    }

    private fun parseListeners(clazz: Class<*>): List<ParsedListener> {
        val parsed = ArrayList<ParsedListener>()
        var type: Class<*> = clazz
        while (type != Any::class.java) {
            for (method in type.declaredMethods) {
                parsed.add(parseMinigameEventMethod(method) ?: continue)
            }
            type = type.superclass
        }
        return parsed
    }

    private fun parseMinigameEventMethod(method: Method): ParsedListener? {
        val event = method.getAnnotation(Listener::class.java) ?: return null
        if (!Modifier.isPrivate(method.modifiers)) {
            throw IllegalArgumentException("Minigame Listener ($method) must be declared private")
        }
        if (method.parameterCount != 1) {
            throw IllegalArgumentException("Minigame Listener ($method) has unexpected parameter count, should be 1")
        }

        val type = method.parameterTypes[0]
        if (!ServerSideEvent::class.java.isAssignableFrom(type)) {
            val message = "Minigame Listener ($method) only accepts parameter type $type but should accept ServerSideEvent"
            throw IllegalArgumentException(message)
        }
        @Suppress("UNCHECKED_CAST")
        type as Class<ServerSideEvent>

        method.isAccessible = true
        return ParsedListener(type, event, MethodHandles.lookup().unreflect(method))
    }

    private class ParsedListener(
        val type: Class<ServerSideEvent>,
        private val event: Listener,
        private val handle: MethodHandle
    ) {
        val filters: Set<ListenerFilter> = ListenerFilter.of(*this.event.filters)

        fun bind(declarer: Any): EventListener<ServerSideEvent> {
            return EventListener.of(this.event.priority, this.event.phase, this.event.strategy) {
                this.handle.invoke(declarer, it)
            }
        }
    }
}