/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.sidebar

import net.minecraft.network.chat.Component

/**
 * A builder for the rows of a [VirtualSidebar].
 *
 * Rows are added bottom-first, so the first row added is displayed
 * at the top of the sidebar.
 */
public class SidebarComponents internal constructor(): Iterable<SidebarComponent> {
    private val rows by lazy { ArrayList<SidebarComponent>(VirtualSidebar.MAX_SIZE) }

    public fun size(): Int {
        return this.rows.size
    }

    public fun getRow(index: Int): SidebarComponent {
        this.checkBounds(index, this.size() - 1)
        return this.rows[index]
    }

    public fun addRow(row: SidebarComponent): SidebarComponents {
        this.addRow(0, row)
        return this
    }

    public fun addRow(component: Component): SidebarComponents {
        this.addRow(SidebarComponent.withNoScore(component))
        return this
    }

    public fun addRow(component: Component, score: Component): SidebarComponents {
        this.addRow(SidebarComponent.withCustomScore(component, score))
        return this
    }

    public fun addRows(rows: Iterable<SidebarComponent>): SidebarComponents {
        for (row in rows) {
            this.addRow(row)
        }
        return this
    }

    public fun addRow(index: Int, row: SidebarComponent): SidebarComponents {
        this.checkBounds(index, this.size())
        this.rows.add(index, row)
        return this
    }

    public fun setRow(index: Int, row: SidebarComponent): SidebarComponents {
        this.checkBounds(index, this.size() - 1)
        this.rows[index] = row
        return this
    }

    public fun removeRow(index: Int): SidebarComponents {
        this.checkBounds(index, this.size() - 1)
        this.rows.removeAt(index)
        return this
    }

    public fun getRows(): List<SidebarComponent> {
        return this.rows
    }

    override fun iterator(): Iterator<SidebarComponent> {
        return this.rows.iterator()
    }

    private fun checkBounds(index: Int, upper: Int) {
        require(index in 0..upper) { "Row index $index out of bounds! Must between 0 and $upper" }
    }

    public companion object {
        public fun empty(): SidebarComponents {
            return SidebarComponents()
        }

        public fun of(vararg components: SidebarComponent): SidebarComponents {
            val instance = empty()
            for (component in components) {
                instance.addRow(component)
            }
            return instance
        }
    }
}
