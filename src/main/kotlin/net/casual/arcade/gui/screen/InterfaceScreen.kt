package net.casual.arcade.gui.screen

import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

abstract class InterfaceScreen(
    inventory: Inventory,
    syncId: Int,
    rows: Int
): AbstractContainerMenu(rowsToType(rows), syncId) {
    private val inventory: Inventory
    private val container: Container

    init {
        this.inventory = inventory
        this.container = SimpleContainer(9 * rows)
        val i = (rows - 4) * 18
        for (j in 0 until rows) {
            for (k in 0..8) {
                this.addSlot(Slot(this.container, k + j * 9, 8 + k * 18, 18 + j * 18))
            }
        }
        for (j in 0..2) {
            for (k in 0..8) {
                this.addSlot(Slot(inventory, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i))
            }
        }
        for (j in 0..8) {
            this.addSlot(Slot(inventory, j, 8 + j * 18, 161 + i))
        }
    }

    constructor(player: Player, syncId: Int, rows: Int): this(Inventory(player), syncId, rows)

    fun getContainer(): Container {
        return this.container
    }

    fun getPlayerInventory(): Inventory {
        return this.inventory
    }

    final override fun clicked(slotId: Int, button: Int, clickType: ClickType, player: Player) {
        this.onClick(slotId, button, clickType, player as ServerPlayer)
    }

    final override fun quickMoveStack(player: Player, slot: Int): ItemStack {
        throw UnsupportedOperationException("Cannot transfer slots from within a CustomScreen")
    }

    final override fun stillValid(player: Player): Boolean {
        return true
    }

    final override fun removed(player: Player) {
        super.removed(player)
        // player.containerMenu.sendAllDataToRemote()
        GlobalTickedScheduler.schedule(0, MinecraftTimeUnit.Ticks, player.containerMenu::sendAllDataToRemote)
    }

    abstract fun onClick(slotId: Int, button: Int, type: ClickType, player: ServerPlayer)

    companion object {
        private fun rowsToType(rows: Int): MenuType<*> {
            return when (rows) {
                1 -> MenuType.GENERIC_9x1
                2 -> MenuType.GENERIC_9x2
                3 -> MenuType.GENERIC_9x3
                4 -> MenuType.GENERIC_9x4
                5 -> MenuType.GENERIC_9x5
                6 -> MenuType.GENERIC_9x6
                else -> throw IllegalStateException("Invalid number of rows: $rows")
            }
        }
    }
}
