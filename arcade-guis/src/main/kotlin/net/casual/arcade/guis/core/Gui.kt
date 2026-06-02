/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.core

import net.casual.arcade.guis.menu.GuiMenu
import net.casual.arcade.guis.utils.getOpenGui
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.Entity
import net.minecraft.world.inventory.MenuType
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.annotations.ApiStatus.OverrideOnly

// What guis do we actually want to support?
//  - Chests
//  - Books
// We can figure the rest out later
public interface Gui {
    public val player: ServerPlayer

    @OverrideOnly
    public fun tick() {

    }

    @OverrideOnly
    public fun valid(): Boolean {
        return true
    }

    @NonExtendable
    public fun open(): Boolean {
        if (this.player.hasDisconnected() || this.isOpen()) {
            return false
        }

        val id = this.player.openMenu(this.createMenuProvider())
        return id.isPresent
    }

    @NonExtendable
    public fun close() {
        if (this.isOpen()) {
            ScopedValue.where(GuiMenu.CLOSE_REASON, CloseReason.Manual)
                .run { this.player.closeContainer() }
        }
    }

    @OverrideOnly
    public fun onOpen() {

    }

    @OverrideOnly
    public fun onClose(reason: CloseReason) {

    }

    @OverrideOnly
    public fun shouldResetMousePosition(): Boolean {
        return false
    }

    @OverrideOnly
    public fun createMenuProvider(): MenuProvider

    public fun getMenuType(): MenuType<*>

    public fun getParent(): Gui?

    public fun setParent(parent: Gui?)

    @NonExtendable
    public fun openParent() {
        this.getParent()?.open()
    }

    @NonExtendable
    public fun openParentOrClose() {
        val parent = this.getParent()
        if (parent == null) {
            this.close()
            return
        }
        parent.open()
    }

    @NonExtendable
    public fun isOpen(): Boolean {
        return this.player.getOpenGui() == this
    }

    public sealed interface CloseReason {
        /**
         * Closed via the [Gui.close] method.
         */
        public data object Manual: CloseReason

        /**
         * Closed by the player, via [ServerboundContainerClosePacket].
         */
        public data object Player: CloseReason

        /**
         * Closed due to [Gui.valid] returning `false`.
         */
        public data object Invalid: CloseReason

        /**
         * The gui was replaced with another menu, [gui] will be
         * non-null if the replacing menu was a [GuiMenu].
         */
        public data class Replaced(val gui: Gui?): CloseReason

        /**
         * Closed due to the player being removed from the world.
         */
        public data class Removed(val reason: Entity.RemovalReason): CloseReason

        /**
         * Closed by some other means.
         */
        public data object Unknown: CloseReason
    }
}