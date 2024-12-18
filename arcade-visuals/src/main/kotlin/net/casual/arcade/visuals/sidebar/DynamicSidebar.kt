/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.sidebar

import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.casual.arcade.visuals.elements.UniversalElement
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

public class DynamicSidebar(title: PlayerSpecificElement<Component>): Sidebar(title) {
    private var rows: PlayerSpecificElement<SidebarComponents<SidebarComponent>>

    init {
        this.rows = UniversalElement.constant(SidebarComponents.empty())
    }

    public fun setRows(rows: PlayerSpecificElement<SidebarComponents<SidebarComponent>>) {
        this.rows = rows
    }

    override fun forEachRow(player: ServerPlayer, consumer: (Int, SidebarComponent) -> Unit) {
        val rows = this.rows.get(player)
        for ((i, component) in rows.getRows().takeLast(MAX_SIZE).withIndex()) {
            consumer.invoke(i, component)
        }
    }

    override fun tick(server: MinecraftServer) {
        this.rows.tick(server)
    }
}