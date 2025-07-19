/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.sidebar

import net.minecraft.network.chat.Component

public class SidebarComponents<C> internal constructor(): Iterable<C> {
    private val rows by lazy { ArrayList<C>(Sidebar.MAX_SIZE) }

    public fun size(): Int {
        return this.rows.size
    }

    public fun getRow(index: Int): C {
        this.checkBounds(index, this.size() - 1)
        return this.rows[index]
    }

    public fun addRow(row: C): SidebarComponents<C> {
        this.addRow(0, row)
        return this
    }

    public fun addRows(rows: Iterable<C>): SidebarComponents<C> {
        for (row in rows) {
            this.addRow(row)
        }
        return this
    }

    public fun addRow(index: Int, row: C): SidebarComponents<C> {
        this.checkBounds(index, this.size())
        this.rows.add(index, row)
        return this
    }

    public fun setRow(index: Int, row: C): SidebarComponents<C> {
        this.checkBounds(index, this.size() - 1)
        this.rows[index] = row
        return this
    }

    public fun removeRow(index: Int): SidebarComponents<C> {
        this.checkBounds(index, this.size() - 1)
        this.rows.removeAt(index)
        return this
    }

    public fun getRows(): List<C> {
        return this.rows
    }

    override fun iterator(): Iterator<C> {
        return this.rows.iterator()
    }

    private fun checkBounds(index: Int, upper: Int) {
        require(index in 0..upper) { "Row index $index out of bounds! Must between 0 and $upper" }
    }

    public companion object {
        public fun empty(): SidebarComponents<SidebarComponent> {
            return SidebarComponents()
        }

        public fun of(vararg components: SidebarComponent): SidebarComponents<SidebarComponent> {
            val instance = empty()
            for (component in components) {
                instance.addRow(component)
            }
            return instance
        }

        public fun SidebarComponents<SidebarComponent>.addRow(
            component: Component
        ): SidebarComponents<SidebarComponent> {
            this.addRow(SidebarComponent.withNoScore(component))
            return this
        }

        public fun SidebarComponents<SidebarComponent>.addRow(
            component: Component,
            score: Component
        ): SidebarComponents<SidebarComponent> {
            this.addRow(SidebarComponent.withCustomScore(component, score))
            return this
        }
    }
}