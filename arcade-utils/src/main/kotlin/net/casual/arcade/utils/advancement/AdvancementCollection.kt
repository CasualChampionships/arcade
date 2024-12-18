/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.advancement

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.minecraft.advancements.AdvancementHolder

public abstract class AdvancementCollection: Collection<AdvancementHolder> {
    private val registered = ObjectLinkedOpenHashSet<AdvancementHolder>()

    final override val size: Int
        get() = this.registered.size

    final override fun isEmpty(): Boolean {
        return this.registered.isEmpty()
    }

    final override fun iterator(): Iterator<AdvancementHolder> {
        return this.registered.iterator()
    }

    final override fun containsAll(elements: Collection<AdvancementHolder>): Boolean {
        return this.registered.containsAll(elements)
    }

    final override fun contains(element: AdvancementHolder): Boolean {
        return this.registered.contains(element)
    }

    protected fun register(builder: AdvancementBuilder.() -> Unit): AdvancementHolder {
        val advancement = AdvancementBuilder.create(builder).build()
        this.registered.add(advancement)
        return advancement
    }
}