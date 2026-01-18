/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.font

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import kotlin.math.roundToInt

public abstract class IndexedFontResources(
    id: ResourceLocation,
    pua: FontPUA = FontPUA.Plane0
): FontResources(id, pua) {
    @PublishedApi internal val components: ArrayList<Component> = ArrayList()

    protected inline fun indexed(component: () -> Component) {
        this.components.add(component.invoke())
    }

    protected open fun get(index: Int): Component {
        return this.components[index]
    }

    public abstract class Batched(
        id: ResourceLocation,
        pua: FontPUA = FontPUA.Plane0
    ): IndexedFontResources(id, pua) {
        private val batches = Int2IntOpenHashMap()
        private var current = 0

        protected fun batch(block: () -> Unit): Batch {
            val start = this.components.size
            block.invoke()
            val end = this.components.size
            val index = this.current++
            batches[index] = end - start
            return Batch(index)
        }

        protected open fun get(batch: Batch, progress: Double): Component {
            val size = this.batches[batch.index]
            val offset = (progress.coerceIn(0.0, 1.0) * (size - 1)).roundToInt()
            return this.get(batch.index + offset)
        }

        protected open fun get(batch: Batch, offset: Int): Component {
            val size = this.batches[batch.index]
            require(offset < size) { "Offset $offset out of bounds for batch ${batch.index} with size $size!" }
            return this.get(batch.index + offset)
        }

        @JvmInline
        public value class Batch(internal val index: Int)
    }
}