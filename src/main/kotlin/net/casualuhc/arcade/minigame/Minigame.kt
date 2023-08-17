package net.casualuhc.arcade.minigame

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.core.Event
import net.casualuhc.arcade.events.player.PlayerEvent
import net.casualuhc.arcade.utils.MinigameUtils.minigame
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.function.Consumer

abstract class Minigame {
    private val connections = HashSet<ServerGamePacketListenerImpl>()

    private val events = EventHandler()

    fun addPlayer(player: ServerPlayer) {
        if (!this.hasPlayer(player) && this.willAcceptPlayer(player)) {
            this.connections.add(player.connection)
            player.minigame.setMinigame(this)
            this.onAddPlayer(player)
        }
    }

    fun removePlayer(player: ServerPlayer) {
        if (this.connections.remove(player.connection)) {
            player.minigame.removeMinigame()
            this.onRemovePlayer(player)
        }
    }

    fun getPlayers(): List<ServerPlayer> {
        return this.connections.map { it.player }
    }

    fun hasPlayer(player: ServerPlayer): Boolean {
        return this.connections.contains(player.connection)
    }

    protected abstract fun willAcceptPlayer(player: ServerPlayer): Boolean

    protected abstract fun onAddPlayer(player: ServerPlayer)

    protected abstract fun onRemovePlayer(player: ServerPlayer)

    protected inline fun <reified T: Event> registerEvent(priority: Int = 1_000, listener: Consumer<T>) {
        this.registerEvent(T::class.java, priority, listener)
    }

    protected fun <T: Event> registerEvent(type: Class<T>, priority: Int = 1_000, listener: Consumer<T>) {
        this.events.register(type, priority, listener)
    }

    protected inline fun <reified T: Event> registerMinigameEvent(priority: Int = 1_000, listener: Consumer<T>) {
        this.registerMinigameEvent(T::class.java, priority, listener)
    }

    protected fun <T: Event> registerMinigameEvent(type: Class<T>, priority: Int = 1_000, listener: Consumer<T>) {
        when {
            registerPredicatedEvent(PlayerEvent::class.java, { this.hasPlayer(it.player) }, type, priority, listener) -> { }
            else -> this.registerEvent(type, priority, listener)
        }
    }

    private fun <T: Event, S: Event> registerPredicatedEvent(
        required: Class<T>,
        predicate: (T) -> Boolean,
        type: Class<S>,
        priority: Int,
        listener: Consumer<S>
    ): Boolean {
        if (required.isAssignableFrom(type)) {
            this.registerEvent(type, priority) {
                @Suppress("UNCHECKED_CAST")
                if (predicate(it as T)) {
                    listener.accept(it)
                }
            }
            return true
        }
        return false
    }
}