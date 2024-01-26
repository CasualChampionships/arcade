package net.casual.arcade.gui.screen

import net.casual.arcade.Arcade
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack

/**
 * This [ArcadeGenericScreen] implementation is a customizable
 * screen that allows you to create functional, clickable, items.
 *
 * The menu provides options for returning to a parent screen as
 * well as setting custom items for the back, next, and filler items.
 *
 * You can create your own selection screens using [SelectionScreenBuilder].
 *
 * @param title The title of the screen.
 * @param selections The possible selections.
 * @param tickers The item tickers.
 * @param player The player opening the screen.
 * @param syncId The sync id.
 * @param parent The parent menu.
 * @param page The page of the screen.
 * @param previous The previous [ItemStack].
 * @param back The back [ItemStack].
 * @param next The next [ItemStack].
 * @param filler The filler [ItemStack].
 * @see SelectionScreenBuilder
 * @see ArcadeGenericScreen
 */
public class SelectionScreen internal constructor(
    private val title: Component,
    private val selections: List<Selection>,
    private val tickers: List<ItemStackTicker>,
    player: Player,
    syncId: Int,
    private val parent: MenuProvider?,
    private val style: SelectionScreenStyle,
    private val page: Int,
    private val previous: ItemStack,
    private val back: ItemStack,
    private val next: ItemStack,
    private val filler: ItemStack
): ArcadeGenericScreen(player, syncId, 6) {
    private val hasNextPage: Boolean

    init {
        val inventory = this.getContainer()
        val slots = this.style.getSlots()
        val count = slots.size

        val paged = this.selections.stream()
            .skip((count * this.page).toLong())
            .limit(count.toLong())
            .toList()

        this.hasNextPage = this.selections.size > count * (this.page + 1)

        for ((i, slot) in slots.withIndex()) {
            inventory.setItem(slot, paged[i].display)
        }

        inventory.setItem(45, this.previous)
        inventory.setItem(49, this.back)
        inventory.setItem(53, this.next)

        for (i in 0 until inventory.containerSize) {
            if (!slots.contains(i) && inventory.getItem(i).isEmpty) {
                inventory.setItem(i, this.filler)
            }
        }
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
    override fun onClick(slotId: Int, button: Int, type: ClickType, player: ServerPlayer) {
        val size = this.getContainer().containerSize
        val next = size - 1
        val back = size - 5
        val previous = size - 9
        if (slotId in previous..next) {
            if (slotId == next && this.hasNextPage) {
                player.openMenu(createScreenFactory(this, this.page + 1))
            } else if (slotId == previous && this.page > 0) {
                player.openMenu(createScreenFactory(this, this.page - 1))
            } else if (slotId == back) {
                if (this.parent != null) {
                    player.openMenu(this.parent)
                } else {
                    player.closeContainer()
                }
            }
            return
        }

        val slot = this.slots.getOrNull(slotId) ?: return
        if (!slot.hasItem()) {
            return
        }

        val selection = this.selections.find { it.display == slot.item }
        if (selection === null) {
            Arcade.logger.warn("SelectionScreen clicked item ${slot.item} but had no action!")
            return
        }
        selection.action(player)
    }

    /**
     * This method is called every server tick.
     *
     * @param server The [MinecraftServer] instance.
     */
    override fun onTick(server: MinecraftServer) {
        val container = this.getContainer()
        for (slot in this.style.getSlots()) {
            val stack = container.getItem(slot)
            if (!stack.isEmpty) {
                this.tickers.forEach { ticker -> ticker.tick(stack) }
            }
        }
    }

    internal class Selection(
        val display: ItemStack,
        val action: (ServerPlayer) -> Unit
    )

    public companion object {
        internal fun createScreenFactory(previous: SelectionScreen, page: Int): SimpleMenuProvider? {
            return createScreenFactory(
                previous.title,
                previous.selections,
                previous.tickers,
                previous.parent,
                previous.style,
                page,
                previous.previous,
                previous.back,
                previous.next,
                previous.filler
            )
        }

        internal fun createScreenFactory(
            title: Component,
            selections: List<Selection>,
            tickers: List<ItemStackTicker>,
            parent: MenuProvider?,
            style: SelectionScreenStyle,
            page: Int,
            previous: ItemStack,
            back: ItemStack,
            next: ItemStack,
            filler: ItemStack,
        ): SimpleMenuProvider? {
            if (page >= 0) {
                return SimpleMenuProvider(
                    { syncId, _, player ->
                        SelectionScreen(title, selections, tickers, player, syncId, parent, style, page, previous, back, next, filler)
                    },
                    title
                )
            }
            return null
        }
    }
}