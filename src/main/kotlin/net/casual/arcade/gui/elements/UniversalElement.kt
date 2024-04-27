package net.casual.arcade.gui.elements

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.NonExtendable

/**
 * This is a sub-interface of [PlayerSpecificElement] which
 * allows you to generate elements that are player independent.
 *
 * @see PlayerSpecificElement
 */
public fun interface UniversalElement<E: Any>: PlayerSpecificElement<E> {
    /**
     * This generates the element.
     *
     * @param server The [MinecraftServer] instance.
     * @return The universal element.
     */
    public fun get(server: MinecraftServer): E

    @NonExtendable
    override fun get(player: ServerPlayer): E {
        return this.get(player.server)
    }

    public companion object {
        /**
         * This creates a wrapper around the provided [universal] that
         * caches the element every tick in order not to constantly
         * re-generate the element.
         *
         * @param universal The element to wrap.
         * @return The cached element generator.
         */
        public fun <E: Any> cached(universal: UniversalElement<E>): UniversalElement<E> {
            return Cached(universal)
        }

        /**
         * This creates a **constant** universal element that will always
         * return the same element.
         *
         * @param element The element to return.
         * @return The [UniversalElement] that returns the [element].
         */
        public fun <E: Any> constant(element: E): UniversalElement<E> {
            return UniversalElement { element }
        }
    }

    /**
     * This is an implementation of [UniversalElement] that
     * caches the universal element to not regenerate the
     * same element for each player which it's called for.
     */
    private class Cached<E: Any>(private val wrapped: UniversalElement<E>): UniversalElement<E> {
        private lateinit var cached: E

        override fun get(server: MinecraftServer): E {
            if (!this::cached.isInitialized) {
                this.cached = this.wrapped.get(server)
            }
            return this.cached
        }

        override fun tick(server: MinecraftServer) {
            this.wrapped.tick(server)
            this.cached = this.wrapped.get(server)
        }
    }
}