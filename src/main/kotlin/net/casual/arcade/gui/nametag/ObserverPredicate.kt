package net.casual.arcade.gui.nametag

import net.minecraft.server.level.ServerPlayer

/**
 * This interface is used to determine whether a given
 * observee can be observed by another player.
 *
 * @see ArcadeNameTag
 */
public fun interface ObserverPredicate {
    /**
     * This method determines whether the [observee] can be
     * observed by the [observer].
     *
     * @param observee The player being observed.
     * @param observer The player observing.
     * @return Whether the [observer] can be observed.
     */
    public fun observable(observee: ServerPlayer, observer: ServerPlayer): Boolean
}