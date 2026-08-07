/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.data

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.virtual.visuals.VirtualVisual
import net.casual.arcade.virtual.visuals.elements.PlayerSpecificElement
import net.casual.arcade.virtual.visuals.elements.TickableElement
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.minecraft.server.MinecraftServer

/**
 * This class binds [PlayerSpecificElement]s to the [PlayerSpecificValue]s
 * of a [VirtualVisual], pulling each element every tick and pushing the
 * generated value back into the visual.
 *
 * Any state shared between elements should be registered with
 * [addTickable], which is ticked before any element is pulled.
 *
 * @see PlayerSpecificValue
 */
public class DynamicVisualValues {
    private val bases = Object2ObjectLinkedOpenHashMap<PlayerSpecificValue<*>, BaseBinding<*>>()
    private val overrides = Object2ObjectLinkedOpenHashMap<PlayerSpecificValue<*>, OverrideBinding<*>>()
    private val tickables = ReferenceLinkedOpenHashSet<TickableElement>()

    /**
     * Registers a [TickableElement] to be ticked before any element is pulled.
     *
     * This should be used for any state shared between elements, so
     * that it is updated exactly once per tick regardless of how many
     * elements read it.
     *
     * @param tickable The tickable to register.
     */
    public fun addTickable(tickable: TickableElement) {
        this.tickables.add(tickable)
    }

    /**
     * Unregisters a previously registered [TickableElement].
     *
     * @param tickable The tickable to unregister.
     */
    public fun removeTickable(tickable: TickableElement) {
        this.tickables.remove(tickable)
    }

    /**
     * Binds the given [element] to the given [value].
     *
     * A [UniversalElement] generates the value's base, any other
     * element generates the value's per-player overrides; binding one
     * replaces only the previous element of that kind, so a value can
     * have both.
     *
     * @param value The value to generate.
     * @param element The element to generate the value with.
     */
    public fun <T: Any> bind(value: PlayerSpecificValue<T>, element: PlayerSpecificElement<out T>) {
        if (element is UniversalElement) {
            this.bases[value] = BaseBinding(value, element)
        } else {
            this.overrides[value] = OverrideBinding(value, element)
        }
    }

    /**
     * Unbinds both elements bound to the given [value], leaving the
     * value at whatever it was last generated as.
     *
     * @param value The value to stop generating.
     */
    public fun unbind(value: PlayerSpecificValue<*>) {
        this.bases.remove(value)
        this.overrides.remove(value)
    }

    /**
     * Ticks every registered [TickableElement].
     *
     * This is separate from [update] because it must happen before
     * *anything* reads an element, including any element the visual
     * pulls itself.
     *
     * @param server The [MinecraftServer] instance.
     */
    public fun tick(server: MinecraftServer) {
        for (tickable in this.tickables) {
            tickable.tick(server)
        }
    }

    /**
     * Pulls every bound element and writes the generated values,
     * writing every base before any override.
     *
     * This should be called after [tick], and before the visual sends
     * its dirty packets.
     *
     * @param server The [MinecraftServer] instance.
     * @param observers The observers of the visual.
     */
    public fun update(server: MinecraftServer, observers: ObserverTracker) {
        for (binding in this.bases.values) {
            binding.update(server)
        }
        for (binding in this.overrides.values) {
            binding.update(server, observers)
        }
    }

    private class BaseBinding<T: Any>(
        private val value: PlayerSpecificValue<T>,
        private val element: UniversalElement<out T>
    ) {
        fun update(server: MinecraftServer) {
            this.element.tick(server)
            this.value.set(this.element.get(server))
        }
    }

    private class OverrideBinding<T: Any>(
        private val value: PlayerSpecificValue<T>,
        private val element: PlayerSpecificElement<out T>
    ) {
        fun update(server: MinecraftServer, observers: ObserverTracker) {
            this.element.tick(server)

            for (observer in observers) {
                val player = observer.asPlayerOrNull() ?: continue
                this.value.set(player.uuid, this.element.get(player))
            }
        }
    }
}
