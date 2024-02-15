package net.casual.arcade.gui.predicate

import net.casual.arcade.minigame.managers.MinigameEffectsManager
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

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
}