package net.casual.arcade.gui.elements

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.NonExtendable
import java.util.UUID

/**
 * This is a functional interface that represents an
 * element that is generated on a per-player basis.
 *
 * Usually this is called multiple times in succession
 * when generating UI elements.
 *
 * @see UniversalElement
 */
public fun interface PlayerSpecificElement<E: Any> {
    /**
     * This generates the element.
     *
     * @param player The player to generate the element for.
     * @return The player-specific element.
     */
    public fun get(player: ServerPlayer): E

    /**
     * This will be called every tick, to update
     * any state used to generate the element.
     *
     * @param server The [MinecraftServer] instance.
     */
    public fun tick(server: MinecraftServer) {

    }

    @NonExtendable
    public fun cached(): PlayerSpecificElement<E> {
        return Cached(this)
    }

    @NonExtendable
    public fun <S: Any, T: Any> merge(
        other: PlayerSpecificElement<S>,
        merger: (a: E, b: S) -> T
    ): PlayerSpecificElement<T> {
        return Merged(this, other, merger)
    }

    private class Cached<E: Any>(private val wrapped: PlayerSpecificElement<E>): PlayerSpecificElement<E> {
        private var cached = Object2ObjectOpenHashMap<UUID, E>()

        override fun get(player: ServerPlayer): E {
            return this.cached.getOrPut(player.uuid) {
                this.wrapped.get(player)
            }
        }

        override fun tick(server: MinecraftServer) {
            this.cached.clear()
            this.wrapped.tick(server)
        }
    }

    private class Merged<A: Any, B: Any, C: Any>(
        private val first: PlayerSpecificElement<A>,
        private val second: PlayerSpecificElement<B>,
        private val merger: (A, B) -> C
    ): PlayerSpecificElement<C> {
        override fun get(player: ServerPlayer): C {
            return this.merger.invoke(this.first.get(player), this.second.get(player))
        }

        override fun tick(server: MinecraftServer) {
            this.first.tick(server)
            this.second.tick(server)
        }
    }
}