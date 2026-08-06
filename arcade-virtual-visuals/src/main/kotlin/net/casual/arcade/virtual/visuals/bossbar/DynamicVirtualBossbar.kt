/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.bossbar

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.virtual.visuals.data.DynamicVisualValues
import net.casual.arcade.virtual.visuals.elements.PlayerSpecificElement
import net.casual.arcade.virtual.visuals.elements.TickableElement
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

/**
 * An extension of [VirtualBossbar] which generates its values from
 * [PlayerSpecificElement]s, instead of having them set directly:
 * ```
 * val bossbar = DynamicVirtualBossbar(server)
 * bossbar.setTitle(ComponentElements.of(Component.literal("Hello!")))
 * bossbar.setProgress { player -> player.health / player.maxHealth }
 * ```
 *
 * Any value without an element bound to it can still be set directly.
 *
 * Generated overrides are discarded when an observer stops observing.
 *
 * @param server The server this bossbar belongs to.
 * @param observers The observer tracker for this bossbar.
 * @see DynamicVisualValues
 */
public open class DynamicVirtualBossbar(
    protected val server: MinecraftServer,
    observers: ObserverTracker = SimpleObserverTracker()
): VirtualBossbar(observers) {
    /**
     * The elements generating this bossbar's values.
     */
    protected val dynamic: DynamicVisualValues = DynamicVisualValues()

    /**
     * Registers a [TickableElement] to be ticked before this bossbar's
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
     * Generates the [progress] with the given [element].
     *
     * @param element The element to generate the progress with.
     */
    public fun setProgress(element: PlayerSpecificElement<out Float>) {
        this.dynamic.bind(this.progress, element)
    }

    /**
     * Generates the [color] with the given [element].
     *
     * @param element The element to generate the color with.
     */
    public fun setColor(element: PlayerSpecificElement<out BossBarColor>) {
        this.dynamic.bind(this.color, element)
    }

    /**
     * Generates the [overlay] with the given [element].
     *
     * @param element The element to generate the overlay with.
     */
    public fun setOverlay(element: PlayerSpecificElement<out BossBarOverlay>) {
        this.dynamic.bind(this.overlay, element)
    }

    /**
     * Generates [dark] with the given [element].
     *
     * @param element The element to generate whether the screen is darkened with.
     */
    public fun setDark(element: PlayerSpecificElement<out Boolean>) {
        this.dynamic.bind(this.dark, element)
    }

    /**
     * Generates [music] with the given [element].
     *
     * @param element The element to generate whether boss music plays with.
     */
    public fun setMusic(element: PlayerSpecificElement<out Boolean>) {
        this.dynamic.bind(this.music, element)
    }

    /**
     * Generates [fog] with the given [element].
     *
     * @param element The element to generate whether there is fog with.
     */
    public fun setFog(element: PlayerSpecificElement<out Boolean>) {
        this.dynamic.bind(this.fog, element)
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
