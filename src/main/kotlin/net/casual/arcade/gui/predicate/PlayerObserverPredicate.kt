package net.casual.arcade.gui.predicate

import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

/**
 * This interface is used to determine whether a given
 * observee can be observed by another player.
 *
 * @see ArcadeNameTag
 */
public fun interface PlayerObserverPredicate: EntityObserverPredicate {
    /**
     * This method determines whether the [observee] can be
     * observed by the [observer].
     *
     * @param observee The player being observed.
     * @param observer The player observing.
     * @return Whether the [observer] can be observed.
     */
    public fun observable(observee: ServerPlayer, observer: ServerPlayer): Boolean

    override fun inverted(): PlayerObserverPredicate {
        return PlayerObserverPredicate { observee, observer ->
            !this.observable(observee, observer)
        }
    }

    override fun and(other: EntityObserverPredicate): PlayerObserverPredicate {
        return PlayerObserverPredicate { observee, observer ->
            this.observable(observee, observer) && other.observable(observee, observer)
        }
    }

    override fun or(other: EntityObserverPredicate): PlayerObserverPredicate {
        return PlayerObserverPredicate { observee, observer ->
            this.observable(observee, observer) || other.observable(observee, observer)
        }
    }

    override fun observable(observee: Entity, observer: ServerPlayer): Boolean {
        return observee is ServerPlayer && this.observable(observee, observer)
    }

    public companion object {
        public fun EntityObserverPredicate.toPlayer(): PlayerObserverPredicate {
            val entity = this
            return PlayerObserverPredicate { observee, observer ->
                entity.observable(observee, observer)
            }
        }
    }
}