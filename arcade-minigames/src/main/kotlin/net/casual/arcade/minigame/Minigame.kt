/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerStopEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.player.*
import net.casual.arcade.minigame.component.MinigameComponents
import net.casual.arcade.minigame.events.*
import net.casual.arcade.minigame.managers.*
import net.casual.arcade.minigame.phase.MinigamePhase
import net.casual.arcade.minigame.managers.MinigamePhaseManager
import net.casual.arcade.minigame.scope.MinigameScopes
import net.casual.arcade.minigame.serialization.MinigameSerializer
import net.casual.arcade.minigame.serialization.SerializableMinigame
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.utils.MinigameResources
import net.casual.arcade.minigame.utils.MinigameResources.Companion.removeFrom
import net.casual.arcade.minigame.utils.MinigameResources.Companion.sendTo
import net.casual.arcade.minigame.utils.MinigameUtils
import net.casual.arcade.scheduler.TickedScheduler
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.storage.ValueOutput
import java.nio.file.Path
import java.util.*
import kotlin.enums.EnumEntries

/**
 * This class represents a [Minigame] which players can play.
 * This is the superclass of all minigames.
 *
 * This implements the bare-bones logic for a minigame and
 * has common utilities used in minigames.
 *
 * Each minigame has its own set of managers which manage
 * all the core functionality of the minigame, see the fields
 * of this class for more information.
 *
 * As well as the minigames own state, see: [phases], [paused].
 * See more info about phases here: [MinigamePhase].
 *
 * You can implement your own minigame by extending this class.
 *
 * @param server The [MinecraftServer] that created the [Minigame].
 * @param phases The complete set of phases this minigame may be in, in order.
 * @see MinigamePhase
 */
public abstract class Minigame(
    /**
     * The [MinecraftServer] that created the [Minigame].
     */
    public val server: MinecraftServer,
    /**
     * The unique id for this minigame.
     */
    public val uuid: UUID,
    /**
     * The [Identifier] of the [Minigame].
     */
    public val id: Identifier,
    /**
     * The phases for this minigame.
     */
    phases: EnumEntries<*>
) {
    private var closing: Boolean = false
    private var completing: Boolean = false

    internal val serializer = MinigameSerializer(this)

    /**
     * This handles registering and invoking events.
     *
     * @see MinigameEventHandler
     */
    public val events: MinigameEventHandler = MinigameEventHandler(this)

    /**
     * This handles all custom minigame components.
     *
     * @see MinigameComponents
     */
    public val components: MinigameComponents = MinigameComponents(this)

    /**
     * Manages the phases for this minigame.
     *
     * @see MinigamePhaseManager
     */
    public val phases: MinigamePhaseManager = MinigamePhaseManager(this, phases)

    /**
     * This handles all the players for this minigame.
     *
     * @see MinigamePlayerManager
     */
    public val players: MinigamePlayerManager = MinigamePlayerManager(this)

    /**
     * This handles all the levels that will be used in the minigame.
     *
     * @see MinigameLevelManager
     */
    public val levels: MinigameLevelManager = MinigameLevelManager(this)

    /**
     * The scopes which own this minigame's tasks, routines,
     * coroutines and listeners.
     *
     * @see MinigameScopes
     */
    public val scopes: MinigameScopes = MinigameScopes(this)

    /**
     * The scheduler for scheduling tasks based on the minigames
     * ticking rate, the scheduler will be paused if the minigame
     * is paused.
     */
    public val scheduler: TickedScheduler get() = this.scopes.root

    /**
     * Manages the tick rate for this minigame.
     */
    public val tickrate: MinigameTickRateManager = MinigameTickRateManager(this)

    /**
     * This manages all the visuals for the minigame.
     *
     * @see MinigameVisualsManager
     */
    public val visuals: MinigameVisualsManager = MinigameVisualsManager(this)

    /**
     * The resource pack manager for packs the minigame requires players to download.
     *
     * @see MinigameResources
     */
    public val resources: MinigameResourceManager = MinigameResourceManager()

    /**
     * This manager is for registering any minigame
     * specific commands, these commands will only be
     * accessible if a player is part of the minigame.
     *
     * @see MinigameCommandManager
     */
    public val commands: MinigameCommandManager = MinigameCommandManager(this)

    /**
     * This manages minigame specific advancements.
     *
     * @see MinigameAdvancementManager
     */
    public val advancements: MinigameAdvancementManager = MinigameAdvancementManager(this)

    /**
     * This manages minigame specific recipes.
     *
     * @see MinigameRecipeManager
     */
    public val recipes: MinigameRecipeManager = MinigameRecipeManager(this)

    /**
     * This manages certain effects for this minigame.
     *
     * @see MinigameEffectsManager
     */
    public val effects: MinigameEffectsManager = MinigameEffectsManager(this)

    /**
     * This manages music for this minigame.
     *
     * @see MinigameMusicManager
     */
    public val music: MinigameMusicManager = MinigameMusicManager(this)

    /**
     * This manages minigame statistics.
     *
     * @see MinigameStatManager
     */
    public val stats: MinigameStatManager = MinigameStatManager()

    /**
     * This manages player minigame tags.
     *
     * @see MinigameTagManager
     */
    public val tags: MinigameTagManager = MinigameTagManager(this)

    /**
     * This manages a minigame's teams.
     *
     * @see MinigameStatManager
     */
    public val teams: MinigameTeamManager = MinigameTeamManager(this)

    /**
     * This manages a minigame's chat.
     *
     * @see MinigameChatManager
     */
    public val chat: MinigameChatManager = MinigameChatManager(this)

    /**
     * This handles all the settings for a minigame.
     */
    public open val settings: MinigameSettings = MinigameSettings(this)

    public var state: MinigameState = MinigameState.Created
        internal set

    /**
     * How long the minigame has been up for.
     * This does not include time that the minigame was paused for.
     */
    public var uptime: Int = 0
        internal set

    /**
     * When minigames are paused, none of the scheduled
     * tasks will execute until the minigame in unpaused.
     *
     * @see pause
     * @see unpause
     */
    public var paused: Boolean = false
        internal set

    /**
     * The current phase of the Minigame, `null`
     * if the minigame isn't in the [MinigameState.Playing] state.
     */
    public val phaseOrNull: MinigamePhase?
        get() = (this.state as? MinigameState.Playing)?.phase

    /**
     * Whether the minigame has initialized.
     */
    public val initialized: Boolean
        get() = this.state is MinigameState.Ready || this.state is MinigameState.Playing

    /**
     * Whether the minigame has started.
     */
    public val started: Boolean
        get() = this.state is MinigameState.Playing

    /**
     * Whether the minigame is closed.
     */
    public val closed: Boolean
        get() = this.state is MinigameState.Closed

    /**
     * Whether the minigame has completed.
     */
    public val completed: Boolean
        get() = this.completing

    /**
     * Whether the minigame is ticking.
     */
    public val ticking: Boolean
        get() = !this.paused && this.started

    /**
     * Starts the minigame.
     */
    public fun start() {
        val state = this.state
        if (state !is MinigameState.Created && state !is MinigameState.Ready) {
            return
        }
        this.tryInitialize()

        val first = this.phases.first()
        this.state = MinigameState.Playing(first)

        GlobalEventHandler.Server.broadcast(MinigameStartEvent(this))

        this.phases.enter(first, null)
    }

    /**
     * This tries to initialize the minigame
     * if it's not already initialized.
     */
    public fun tryInitialize() {
        if (this.state is MinigameState.Closed) {
            throw IllegalStateException("Cannot initialize closed minigame ${this.id}")
        }
        if (this.state is MinigameState.Created) {
            this.initialize(resuming = false)
        }
    }

    /**
     * This will pause the minigame, stopping the scheduler
     * from executing any more tasks.
     * This will also broadcast the [MinigamePauseEvent].
     *
     * @see paused
     */
    public fun pause() {
        if (!this.paused) {
            this.paused = true
            GlobalEventHandler.Server.broadcast(MinigamePauseEvent(this))
        }
    }

    /**
     * This will unpause the minigame, resuming the scheduler.
     * This will also broadcast the [MinigameUnpauseEvent].
     *
     * @see paused
     */
    public fun unpause() {
        if (this.paused) {
            this.paused = false
            GlobalEventHandler.Server.broadcast(MinigameUnpauseEvent(this))
        }
    }

    /**
     * Ends the minigame, the difference between [close] and
     * [complete] is that this is only called after the minigame
     * is that this is only called after the minigame is considered
     * to be in its finished state.
     *
     * This should **only** be called the minigame implementation
     * to signify when it has naturally ended.
     *
     * @see close
     */
    public fun complete() {
        this.close(completed = true)
    }

    /**
     * This closes the minigame, all players are removed from the
     * minigame, all tasks are cleared, and all events are unregistered.
     *
     * This also broadcasts the [MinigameCloseEvent] **before** all the players
     * have been removed.
     *
     * After a minigame has been closed, no more players are permitted to join.
     *
     * If your minigame has finished naturally you should call [complete] instead.
     *
     * @see complete
     */
    @JvmOverloads
    public fun close(completed: Boolean = false) {
        if (this.closing || this.state is MinigameState.Closed) {
            return
        }
        this.closing = true
        this.completing = completed

        if (completed) {
            GlobalEventHandler.Server.broadcast(MinigameCompleteEvent(this))
        }

        this.scopes.cancelAll()

        GlobalEventHandler.Server.broadcast(MinigameCloseEvent(this))
        this.players.close()
        this.levels.close()

        // Closed only after the players have been removed
        this.state = MinigameState.Closed(completed)

        this.components.close()
        this.scopes.close()

        GlobalEventHandler.Server.removeProvider(this.events)
        this.events.clear()

        Minigames.unregister(this)
    }

    override fun toString(): String {
        return "${this::class.java.simpleName}[id=${this.id}, uuid=${this.uuid}]"
    }

    /**
     * Gets the save path for this minigame.
     *
     * @return The path where the minigame is saved.
     */
    public fun getSavePath(): Path {
        return Minigames.getInstancesSavePath(this.server)
            .resolve("${this.id.namespace}.${this.id.path}")
            .resolve(this.uuid.toString())
    }

    public open fun debug(output: ValueOutput) {
        output.putString("type", this::class.java.simpleName)
        output.putString("id", this.id.toString())
        output.putString("uuid", this.uuid.toString())
        output.putInt("uptime", this.uptime)
        output.putBoolean("initialized", this.initialized)
        output.putBoolean("serializable", this is SerializableMinigame)
        output.putBoolean("ticking", this.ticking)
        output.putBoolean("paused", this.paused)
        output.putString("state", this.state.toString())

        this.players.debug(output.child("players"))
        this.teams.debug(output.child("teams"))
        this.chat.debug(output.child("chat"))
        this.phases.debug(output.child("phases"))
        this.levels.debug(output.child("levels"))
        this.settings.debug(output.child("settings"))
        this.advancements.debug(output.child("advancements"))
        this.recipes.debug(output.child("recipes"))
        this.commands.debug(output.child("commands"))
    }

    internal fun tryRestore() {
        if (this.state is MinigameState.Closed) {
            throw IllegalStateException("Cannot initialize closed minigame ${this.id}")
        }
        if (this.state is MinigameState.Created) {
            this.initialize(resuming = true)
        }
    }

    /**
     * This method initializes the core functionality of the
     * minigame, such as registering events.
     */
    private fun initialize(resuming: Boolean) {
        this.registerEvents()
        GlobalEventHandler.Server.addProvider(this.events)
        this.tickrate.initialize()
        this.levels.initialize()
        this.settings.initialize()
        MinigameUtils.parseMinigameEvents(this)

        this.components.initialize()

        Minigames.register(this)

        this.state = MinigameState.Ready

        if (!resuming) {
            this.broadcastInitializeEvent()
            this.broadcastLoadEvent()
        }
    }

    private fun broadcastInitializeEvent() {
        GlobalEventHandler.Server.broadcast(MinigameInitializeEvent(this))
    }

    internal fun broadcastLoadEvent() {
        GlobalEventHandler.Server.broadcast(MinigameLoadEvent(this))
    }

    private fun registerEvents() {
        this.events.register<ServerTickEvent> { this.onServerTick(it) }
        this.events.register<PlayerAttackEvent> { this.onPlayerAttack(it) }
        this.events.register<PlayerEntityInteractionEvent> { this.onPlayerEntityInteraction(it) }
        this.events.register<MinigameAddPlayerEvent>(Int.MAX_VALUE) { this.onPlayerAdd(it) }
        this.events.register<MinigameRemovePlayerEvent>(2000) { this.onPlayerRemove(it) }
        this.events.register<ServerStopEvent> { this.onServerStopping() }
    }

    private fun onServerTick(event: ServerTickEvent) {
        this.tickrate.tick()
        this.visuals.tick()
        if (this.ticking) {
            this.uptime++
            this.scopes.tick()
            this.phases.tick()
        }
    }

    private fun onPlayerAdd(event: MinigameAddPlayerEvent) {
        this.resources.sendTo(event.player)
        this.tickrate.updateJoiningPlayer(event.player)
    }

    private fun onPlayerRemove(event: MinigameRemovePlayerEvent) {
        this.resources.removeFrom(event.player)
    }

    private fun onPlayerAttack(event: PlayerAttackEvent) {
        if (!this.settings.canAttackEntities.get(event.player)) {
            event.cancel()
        }
    }

    private fun onPlayerEntityInteraction(event: PlayerEntityInteractionEvent) {
        if (!this.settings.canInteractEntities.get(event.player)) {
            event.cancel(InteractionResult.FAIL)
        }
    }

    private fun onServerStopping() {
        if (this.settings.pauseOnServerStop && !this.paused && this.started) {
            this.pause()
        }
    }
}