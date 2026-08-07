/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.sidebar

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.network.PacketSender
import net.casual.arcade.utils.scoreboard.DummyScoreboard
import net.casual.arcade.virtual.visuals.VirtualVisual
import net.casual.arcade.virtual.visuals.data.PlayerSpecificValue
import net.casual.arcade.virtual.visuals.data.PlayerSpecificVisualData
import net.casual.arcade.virtual.visuals.utils.stopObservingAndSendPackets
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundResetScorePacket
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket.METHOD_ADD
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket.METHOD_CHANGE
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket.METHOD_REMOVE
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.DisplaySlot
import java.util.*

/**
 * A sidebar implementation of [VirtualVisual].
 *
 * The [title] and each of the [MAX_SIZE] rows have a base value, which
 * all observers are shown by default, as well as optional per-player
 * overrides:
 * ```
 * val sidebar = VirtualSidebar()
 * sidebar.title.set(Component.literal("Scores"))
 * sidebar.setRows(SidebarComponents.of(first, second))
 *
 * sidebar.row(0).set(player, SidebarComponent.withNoScore(Component.literal("You!")))
 * ```
 *
 * Row `0` is the bottom row of the sidebar. Rows default to
 * [SidebarComponent.NONE] and are not displayed until they are set;
 * the sidebar is as tall as its highest set row, and any unset row
 * below that is displayed blank.
 *
 * An observer can only be shown one sidebar at a time; observing this
 * sidebar will stop them observing whichever sidebar they were shown
 * before.
 *
 * @param observers The observer tracker for this sidebar.
 * @see PlayerSpecificValue
 */
public open class VirtualSidebar(
    override val observers: ObserverTracker = SimpleObserverTracker()
): VirtualVisual {
    /**
     * The data for this sidebar.
     */
    protected val data: PlayerSpecificVisualData = PlayerSpecificVisualData()

    /**
     * The title of the sidebar.
     */
    public val title: PlayerSpecificValue<Component> = this.data.register(CommonComponents.EMPTY)

    private val rows: List<PlayerSpecificValue<SidebarComponent>> = List(MAX_SIZE) {
        this.data.register(SidebarComponent.NONE)
    }

    /**
     * Gets the row at the given [index], where `0` is the bottom row.
     *
     * @param index The index of the row.
     * @return The row.
     */
    public fun row(index: Int): PlayerSpecificValue<SidebarComponent> {
        require(index in 0..< MAX_SIZE) { "Row index $index out of bounds! Must be between 0 and ${MAX_SIZE - 1}" }
        return this.rows[index]
    }

    /**
     * Sets the base rows of the sidebar, clearing any row beyond
     * those given.
     *
     * If more than [MAX_SIZE] rows are given, only the last [MAX_SIZE]
     * are displayed.
     *
     * @param rows The rows to display.
     */
    public fun setRows(rows: List<SidebarComponent>) {
        val displayed = rows.takeLast(MAX_SIZE)
        for (index in 0..< MAX_SIZE) {
            this.rows[index].set(displayed.getOrElse(index) { SidebarComponent.NONE })
        }
    }

    /**
     * Sets the base rows of the sidebar.
     *
     * @param rows The rows to display.
     * @see setRows
     */
    public fun setRows(rows: SidebarComponents) {
        this.setRows(rows.getRows())
    }

    /**
     * Overrides the rows of the sidebar for the given [player],
     * clearing any row beyond those given.
     *
     * @param player The uuid of the player to override for.
     * @param rows The rows to display to the given [player].
     */
    public fun setRows(player: UUID, rows: List<SidebarComponent>) {
        val displayed = rows.takeLast(MAX_SIZE)
        for (index in 0..< MAX_SIZE) {
            this.rows[index].set(player, displayed.getOrElse(index) { SidebarComponent.NONE })
        }
    }

    /**
     * Overrides the rows of the sidebar for the given [player].
     *
     * @param player The player to override for.
     * @param rows The rows to display to the given [player].
     * @see setRows
     */
    public fun setRows(player: ServerPlayer, rows: List<SidebarComponent>) {
        this.setRows(player.uuid, rows)
    }

    /**
     * Removes the given [player]'s row overrides, so that they are
     * shown the base rows again.
     *
     * @param player The uuid of the player to remove the overrides for.
     */
    public fun setRowsToBase(player: UUID) {
        for (row in this.rows) {
            row.setToBase(player)
        }
    }

    /**
     * Removes the given [player]'s row overrides, so that they are
     * shown the base rows again.
     *
     * @param player The player to remove the overrides for.
     * @see setRowsToBase
     */
    public fun setRowsToBase(player: ServerPlayer) {
        this.setRowsToBase(player.uuid)
    }

    override fun tick() {
        val base = this.data.clean()
        this.observers.broadcast { observer ->
            this.sendDirtyPackets(observer, base)
        }
    }

    override fun sendSpawnPackets(observer: Observer, sender: PacketSender) {
        sender.send(createSetObjectivePacket(METHOD_ADD, this.title.get(observer)))
        sender.send(createSetDisplayPacket(false))

        for (index in 0..< this.getDisplayedSize(observer)) {
            sender.send(createSetScorePacket(index, this.getDisplayedRow(index, observer)))
        }
    }

    override fun sendDespawnPackets(observer: Observer, sender: PacketSender) {
        sender.send(createSetObjectivePacket(METHOD_REMOVE))
        sender.send(createSetDisplayPacket(true))
    }

    override fun onStartObserving(observer: Observer) {
        val player = observer.asPlayerOrNull()
        if (player != null) {
            this.data.clean(player.uuid)
        }

        val current = observer.context.get(CURRENT_SIDEBAR)
        if (current != null && current != this) {
            current.stopObservingAndSendPackets(observer)
        }
        observer.context.set(CURRENT_SIDEBAR, this)
    }

    override fun onStopObserving(observer: Observer) {
        if (observer.context.get(CURRENT_SIDEBAR) == this) {
            observer.context.remove(CURRENT_SIDEBAR)
        }
    }

    /**
     * Sends the given [observer] the packets for any of this sidebar's
     * values which have changed for them since the last tick.
     *
     * @param observer The observer to send packets to.
     * @param baseDirty The mask returned by [PlayerSpecificVisualData.clean].
     */
    protected open fun sendDirtyPackets(observer: Observer, baseDirty: Int) {
        val player = observer.asPlayerOrNull()
        val dirty = if (player !== null) this.data.clean(player.uuid, baseDirty) else baseDirty
        if (dirty == 0) {
            return
        }

        if (dirty and this.title.bit != 0) {
            observer.send(createSetObjectivePacket(METHOD_CHANGE, this.title.get(observer)))
        }

        val size = this.getDisplayedSize(observer)
        var cleared = false
        for (index in 0..< MAX_SIZE) {
            if (dirty and this.rows[index].bit == 0) {
                continue
            }
            if (index < size) {
                observer.send(createSetScorePacket(index, this.getDisplayedRow(index, observer)))
            } else {
                cleared = true
            }
        }

        if (cleared) {
            // Clearing a row may have shrunk the sidebar past rows which
            // aren't dirty themselves, so the whole tail is reset
            for (index in size..< MAX_SIZE) {
                observer.send(createResetScorePacket(index))
            }
        }
    }

    /**
     * Gets the number of rows the given [observer] is displayed, which
     * is one more than the index of their highest set row.
     *
     * @param observer The observer.
     * @return The number of displayed rows.
     */
    protected fun getDisplayedSize(observer: Observer): Int {
        for (index in (MAX_SIZE - 1) downTo 0) {
            if (this.rows[index].get(observer) != SidebarComponent.NONE) {
                return index + 1
            }
        }
        return 0
    }

    /**
     * Gets the row the given [observer] is displayed at the given
     * [index], where an unset row is displayed blank.
     *
     * @param index The index of the row.
     * @param observer The observer.
     * @return The row to display.
     */
    protected fun getDisplayedRow(index: Int, observer: Observer): SidebarComponent {
        val row = this.rows[index].get(observer)
        return if (row == SidebarComponent.NONE) SidebarComponent.EMPTY else row
    }

    public companion object {
        /**
         * The maximum number of rows a sidebar can display.
         */
        public const val MAX_SIZE: Int = 15

        private const val OBJECTIVE_NAME = $$"Z$DummyObjective"

        private val objective = DummyScoreboard.objective(OBJECTIVE_NAME)
        private val owners = Array(MAX_SIZE) { index -> $$"$D$${index.toString(16)}" }

        private val CURRENT_SIDEBAR = Observer.Context.Key<VirtualSidebar>(arcade("virtual_sidebar"))

        private fun createSetObjectivePacket(method: Int, title: Component? = null): Packet<*> {
            if (title !== null) {
                objective.displayName = title
            }
            return ClientboundSetObjectivePacket(objective, method)
        }

        private fun createSetScorePacket(index: Int, component: SidebarComponent): Packet<*> {
            return ClientboundSetScorePacket(
                owners[index],
                OBJECTIVE_NAME,
                index,
                Optional.ofNullable(component.display),
                Optional.ofNullable(component.score)
            )
        }

        private fun createResetScorePacket(index: Int): Packet<*> {
            return ClientboundResetScorePacket(owners[index], OBJECTIVE_NAME)
        }

        private fun createSetDisplayPacket(remove: Boolean): Packet<*> {
            return ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, if (remove) null else objective)
        }
    }
}
