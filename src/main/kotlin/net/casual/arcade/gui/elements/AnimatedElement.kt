package net.casual.arcade.gui.elements

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.casual.arcade.scheduler.MinecraftTimeDuration

public class AnimatedElement<E: Any> private constructor(
    private val elements: Int2ObjectMap<E>,
    private val duration: MinecraftTimeDuration,
    private var current: E
) {
    private var tick = 0

    public fun tick() {
        val tick = ++this.tick % this.duration.ticks
        val next = this.elements.get(tick)
        if (next != null) {
            this.current = next
        }
    }

    public fun get(): E {
        return this.current
    }

    public class Builder<E: Any> {
        private val elements = Int2ObjectLinkedOpenHashMap<E>()
        private var duration = MinecraftTimeDuration.ZERO

        public fun add(element: E, duration: MinecraftTimeDuration) {
            this.elements.put(this.duration.ticks, element)
            this.duration += duration
        }

        public fun build(): AnimatedElement<E> {
            val first = this.elements.get(0) ?: throw IllegalStateException("You must add at least one element to an animated element")
            return AnimatedElement(Int2ObjectLinkedOpenHashMap(this.elements), this.duration, first)
        }
    }
}