package net.casual.arcade.visuals.sidebar

import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.casual.arcade.visuals.extensions.PlayerSidebarExtension.Companion.sidebar
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

public class FixedSidebar(title: PlayerSpecificElement<Component>): Sidebar(title) {
    private val rows = SidebarComponents<PlayerSpecificElement<SidebarComponent>>()

    public fun size(): Int {
        return this.rows.size()
    }

    public fun getRow(index: Int): PlayerSpecificElement<SidebarComponent> {
        return this.rows.getRow(index)
    }

    public fun addRow(row: PlayerSpecificElement<SidebarComponent>) {
        this.addRow(0, row)
    }

    public fun addRow(index: Int, row: PlayerSpecificElement<SidebarComponent>) {
        this.rows.addRow(index, row)

        for (player in this.getPlayers()) {
            player.sidebar.addRow(index, row.get(player))
        }
    }

    public fun setRow(index: Int, row: PlayerSpecificElement<SidebarComponent>) {
        this.rows.setRow(index, row)

        for (player in this.getPlayers()) {
            player.sidebar.setRow(index, row.get(player))
        }
    }

    public fun removeRow(index: Int) {
        this.rows.removeRow(index)

        for (player in this.getPlayers()) {
            player.sidebar.removeRow(index)
        }
    }

    override fun forEachRow(player: ServerPlayer, consumer: (Int, SidebarComponent) -> Unit) {
        for ((i ,row) in this.rows.getRows().takeLast(MAX_SIZE).withIndex()) {
            consumer.invoke(i, row.get(player))
        }
    }

    override fun tick(server: MinecraftServer) {
        for (row in this.rows) {
            row.tick(server)
        }
    }
}