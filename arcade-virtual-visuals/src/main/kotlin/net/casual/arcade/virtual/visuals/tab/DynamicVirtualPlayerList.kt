/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.tab

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.virtual.visuals.data.DynamicVisualValues
import net.casual.arcade.virtual.visuals.elements.PlayerSpecificElement
import net.casual.arcade.virtual.visuals.elements.TickableElement
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

/**
 * An extension of [VirtualPlayerList] which generates its [header] and
 * [footer] from [PlayerSpecificElement]s, instead of having them set
 * directly:
 * ```
 * val list = DynamicVirtualPlayerList(server, VanillaPlayerListEntries())
 * list.setHeader(ComponentElements.of(Component.literal("Welcome!")))
 * list.setFooter(TPSComponentElement)
 * ```
 *
 * Generated overrides are discarded when an observer stops observing.
 *
 * @param server The server this player list belongs to.
 * @param entries The entries to display in the list.
 * @param observers The observer tracker for this player list.
 * @see DynamicVisualValues
 */
public open class DynamicVirtualPlayerList(
    server: MinecraftServer,
    entries: PlayerListEntries,
    observers: ObserverTracker = SimpleObserverTracker()
): VirtualPlayerList(server, entries, observers) {
    /**
     * The elements generating this player list's values.
     */
    protected val dynamic: DynamicVisualValues = DynamicVisualValues()

    /**
     * Registers a [TickableElement] to be ticked before this player list's
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
     * Generates the [header] with the given [element].
     *
     * @param element The element to generate the header with.
     */
    public fun setHeader(element: PlayerSpecificElement<out Component>) {
        this.dynamic.bind(this.header, element)
    }

    /**
     * Generates the [footer] with the given [element].
     *
     * @param element The element to generate the footer with.
     */
    public fun setFooter(element: PlayerSpecificElement<out Component>) {
        this.dynamic.bind(this.footer, element)
    }

    override fun tick() {
        this.dynamic.tick(this.server)
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
}
