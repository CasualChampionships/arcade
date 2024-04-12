package net.casual.arcade.minigame.module

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.minigame.managers.MinigameEventHandler
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.phase.Phased
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.Experimental
import java.util.*

@Experimental
public abstract class MinigameModule<M: Minigame<M>, P: MinigameModule<M, P>>(
    public val minigame: M
): Phased<P>, MinigameEventListener {
    internal val players: MutableSet<UUID>
    internal val phases: List<Phase<P>>

    // TODO: Scheduler

    public val events: MinigameEventHandler<P>

    public final override var phase: Phase<P>
        internal set

    public abstract val id: ResourceLocation

    init {
        this.players = LinkedHashSet()
        this.phases = ArrayList()

        val self = this.cast()
        this.events = MinigameEventHandler(self, object: MinigameEventHandler.Filterer(this.minigame) {
            override fun hasPlayer(player: ServerPlayer): Boolean {
                return self.hasPlayer(player)
            }
        })

        this.phase = Phase.none()
    }

    public fun addPlayer(player: ServerPlayer): Boolean {
        if (!this.minigame.players.has(player) || !this.minigame.players.isPlaying(player)) {
            return false
        }
        return this.players.add(player.uuid)
    }

    public fun removePlayer(player: ServerPlayer): Boolean {
        return this.players.remove(player.uuid)
    }

    public fun hasPlayer(player: ServerPlayer): Boolean {
        return this.players.contains(player.uuid)
    }

    protected fun cast(): P {
        @Suppress("UNCHECKED_CAST")
        return this as P
    }
}