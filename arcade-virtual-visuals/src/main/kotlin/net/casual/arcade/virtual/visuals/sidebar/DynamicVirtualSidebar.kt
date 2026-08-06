/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.sidebar

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.virtual.visuals.data.DynamicVisualValues
import net.casual.arcade.virtual.visuals.elements.PlayerSpecificElement
import net.casual.arcade.virtual.visuals.elements.TickableElement
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

/**
 * An extension of [VirtualSidebar] which generates its values from
 * [PlayerSpecificElement]s, instead of having them set directly:
 * ```
 * val sidebar = DynamicVirtualSidebar(server)
 * sidebar.setTitle(ComponentElements.of(Component.literal("Scores")))
 * sidebar.setRows { player -> SidebarComponents.of(scoreOf(player)) }
 * ```
 *
 * A whole-sidebar element, set with [setRows], is generated before any
 * individual row element, set with [setRow], so an individual row
 * always wins.
 *
 * Every value may have both a [UniversalElement] generating its base
 * and another element generating its per-player overrides, so a single
 * sidebar can show shared rows to most observers and extra rows to the
 * players which need them.
 *
 * Generated overrides are discarded when an observer stops observing.
 *
 * @param server The server this sidebar belongs to.
 * @param observers The observer tracker for this sidebar.
 * @see DynamicVisualValues
 */
public open class DynamicVirtualSidebar(
    protected val server: MinecraftServer,
    observers: ObserverTracker = SimpleObserverTracker()
): VirtualSidebar(observers) {
    /**
     * The elements generating this sidebar's values.
     */
    protected val dynamic: DynamicVisualValues = DynamicVisualValues()

    private var base: UniversalElement<out SidebarComponents>? = null
    private var overrides: PlayerSpecificElement<out SidebarComponents>? = null

    /**
     * Registers a [TickableElement] to be ticked before this sidebar's
     * elements are pulled.
     *
     * @param tickable The tickable to register.
     */
    public fun addTickable(tickable: TickableElement) {
        this.dynamic.addTickable(tickable)
    }

    /**
     * Unregisters a previously registered [TickableElement].
     *
     * @param tickable The tickable to unregister.
     */
    public fun removeTickable(tickable: TickableElement) {
        this.dynamic.removeTickable(tickable)
    }

    /**
     * Generates the [title] with the given [element].
     *
     * @param element The element to generate the title with.
     */
    public fun setTitle(element: PlayerSpecificElement<out Component>) {
        this.dynamic.bind(this.title, element)
    }

    /**
     * Generates the row at the given [index] with the given [element].
     *
     * @param index The index of the row, where `0` is the bottom row.
     * @param element The element to generate the row with.
     */
    public fun setRow(index: Int, element: PlayerSpecificElement<out SidebarComponent>) {
        this.dynamic.bind(this.row(index), element)
    }

    /**
     * Generates all the rows of the sidebar with the given [element],
     * clearing any row beyond those it generates.
     *
     * A [UniversalElement] generates the base rows, any other element
     * generates the per-player rows; setting one replaces only the
     * previous element of that kind, so a sidebar can have both.
     *
     * @param element The element to generate the rows with.
     */
    public fun setRows(element: PlayerSpecificElement<out SidebarComponents>) {
        if (element is UniversalElement) {
            this.base = element
        } else {
            this.overrides = element
        }
    }

    override fun tick() {
        this.dynamic.tick(this.server)
        this.updateRows()
        this.dynamic.update(this.server, this.observers)

        super.tick()
    }

    override fun onStopObserving(observer: Observer) {
        super.onStopObserving(observer)

        val player = observer.asPlayerOrNull()
        if (player != null) {
            this.data.remove(player.uuid)
        }
    }

    private fun updateRows() {
        val base = this.base
        if (base != null) {
            base.tick(this.server)
            this.setRows(base.get(this.server).getRows())
        }

        val overrides = this.overrides
        if (overrides != null) {
            overrides.tick(this.server)
            for (observer in this.observers) {
                val player = observer.asPlayerOrNull() ?: continue
                this.setRows(player.uuid, overrides.get(player).getRows())
            }
        }
    }
}
