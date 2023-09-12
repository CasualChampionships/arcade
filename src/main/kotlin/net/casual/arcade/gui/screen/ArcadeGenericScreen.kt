package net.casual.arcade.gui.screen

import net.casual.arcade.events.SingleEventHandler
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.EventUtils.registerHandler
import net.casual.arcade.utils.EventUtils.unregisterHandler
import net.casual.arcade.utils.TimeUtils.Ticks
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

/**
 * This class represents a generic custom server-sided screen.
 *
 * This screen uses the 'chest' UI, and you can specify the number
 * of rows you would like the screen to have.
 *
 * @param inventory The player's inventory.
 * @param syncId The syncId provided by the [MenuProvider].
 * @param rows The number of rows the screen should have.
 * @see SelectionScreenBuilder
 * @see SpectatorUsableScreen
 * @see constructor
 */
abstract class ArcadeGenericScreen(
    inventory: Inventory,
    syncId: Int,
    rows: Int
): AbstractContainerMenu(rowsToType(rows), syncId), SpectatorUsableScreen {
    private val ticking: SingleEventHandler<ServerTickEvent>

    private val inventory: Inventory
    private val container: Container

    init {
        this.ticking = SingleEventHandler.of { (server) ->
            this.onTick(server)
        }

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

        this.ticking.registerHandler()
    }

    /**
     * This constructs a [ArcadeGenericScreen] using a given player's actual
     * inventory as the actual inventory.
     *
     * @param player The player whose inventory to use.
     * @param syncId The syncId provided by the [MenuProvider].
     * @param rows The number of rows the screen should have.
     */
    constructor(player: Player, syncId: Int, rows: Int): this(Inventory(player), syncId, rows)

    /**
     * This method gets the main inventory container of this screen.
     *
     * @return The inventory container.
     */
    fun getContainer(): Container {
        return this.container
    }

    /**
     * This method gets the player inventory of the screen.
     *
     * @return The player inventory.
     */
    fun getPlayerInventory(): Inventory {
        return this.inventory
    }

    /**
     * This method is called when a slot is clicked on the screen.
     *
     * This could be a slot in either the main inventory container or
     * the player's inventory.
     *
     * This also gets invoked when the player tries to drop an item
     * outside the inventory, in which case [slotId] is -999.
     *
     * @param slotId The id of the slot that was clicked.
     * @param button The button id, used for a left and right-click, or the swapped slot.
     * @param type The type of click the player did.
     * @param player The player that clicked.
     */
    abstract fun onClick(slotId: Int, button: Int, type: ClickType, player: ServerPlayer)

    /**
     * This method is called when the screen is removed or closed.
     *
     * @param player The player who had the screen open.
     */
    open fun onRemove(player: ServerPlayer) {

    }

    /**
     * This method is called every server tick.
     *
     * @param server The [MinecraftServer] instance.
     */
    open fun onTick(server: MinecraftServer) {

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
        this.onRemove(player as ServerPlayer)
        super.removed(player)
        this.ticking.unregisterHandler()
        GlobalTickedScheduler.schedule(1.Ticks, player.containerMenu::sendAllDataToRemote)
    }

    companion object {
        fun rowsToType(rows: Int): MenuType<*> {
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
