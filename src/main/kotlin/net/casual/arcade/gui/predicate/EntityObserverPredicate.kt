package net.casual.arcade.gui.predicate

import net.casual.arcade.minigame.managers.MinigameEffectsManager
import net.casual.arcade.utils.PlayerUtils.isGameMode
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.GameType

/**
 * This interface is used to determine whether a given
 * entity observee can be observed by another player.
 *
 * @see MinigameEffectsManager
 */
public fun interface EntityObserverPredicate {
    /**
     * This method determines whether the [observee] can be
     * observed by the [observer].
     *
     * @param observee The entity being observed.
     * @param observer The player observing.
     * @return Whether the [observer] can be observed.
     */
    public fun observable(observee: Entity, observer: ServerPlayer): Boolean

    public fun inverted(): EntityObserverPredicate {
        return EntityObserverPredicate { observee, observer ->
            !this.observable(observee, observer)
        }
    }

    public fun and(other: EntityObserverPredicate): EntityObserverPredicate {
        return EntityObserverPredicate { observee, observer ->
            this.observable(observee, observer) && other.observable(observee, observer)
        }
    }

    public fun or(other: EntityObserverPredicate): EntityObserverPredicate {
        return EntityObserverPredicate { observee, observer ->
            this.observable(observee, observer) || other.observable(observee, observer)
        }
    }

    public companion object {
        private val FALSE = EntityObserverPredicate { _, _ -> false }
        private val TRUE = EntityObserverPredicate { _, _ -> true }
        private val VISIBLE_OBSERVEE = EntityObserverPredicate { observee, _ -> !observee.isInvisible }
        private val INVISIBLE_OBSERVER = EntityObserverPredicate { _, observer -> observer.isInvisible }
        private val TEAMMATES = EntityObserverPredicate { observee, observer ->
            observee.team != null && observee.team == observer.team
        }

        public fun ofType(type: EntityType<*>): EntityObserverPredicate {
            return EntityObserverPredicate { observee, _ -> observee.type == type }
        }

        public fun withinRadius(radius: Double): EntityObserverPredicate {
            return EntityObserverPredicate { observee, observer ->
                observee.distanceToSqr(observer) < radius * radius
            }
        }

        public fun observerInGamemode(gamemode: GameType): EntityObserverPredicate {
            return EntityObserverPredicate { _, observer -> observer.isGameMode(gamemode) }
        }

        public fun visibleObservee(): EntityObserverPredicate {
            return VISIBLE_OBSERVEE
        }

        public fun invisibleObserver(): EntityObserverPredicate {
            return INVISIBLE_OBSERVER
        }

        public fun teammates(): EntityObserverPredicate {
            return TEAMMATES
        }

        public fun always(): EntityObserverPredicate {
            return TRUE
        }

        public fun never(): EntityObserverPredicate {
            return FALSE
        }
    }
}