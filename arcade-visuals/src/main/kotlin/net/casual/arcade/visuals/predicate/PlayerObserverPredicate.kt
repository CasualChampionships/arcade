/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.predicate

import net.casual.arcade.visuals.nametag.PlayerNameTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

/**
 * This interface is used to determine whether a given
 * observee can be observed by another player.
 *
 * @see PlayerNameTag
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
            return PlayerObserverPredicate { observee, observer ->
                this.observable(observee, observer)
            }
        }
    }
}