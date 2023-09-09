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

class SelectionScreen internal constructor(
    private val title: Component,
    private val selections: List<Selection>,
    private val tickers: List<ItemStackTicker>,
    player: Player,
    syncId: Int,
    private val parent: MenuProvider? = null,
    private val page: Int,
    private val previous: ItemStack,
    private val back: ItemStack,
    private val next: ItemStack,
    private val filler: ItemStack
): InterfaceScreen(player, syncId, 6) {
    private val lastSelectionSlot: Int

    init {
        val inventory = this.getContainer()
        val size = inventory.containerSize
        val selectionSize = size - 9

        val paged = this.selections.stream()
            .skip((selectionSize * page).toLong())
            .limit(selectionSize.toLong())
            .toList()

        var slot = 0
        for (selection in paged) {
            inventory.setItem(slot++, selection.display)
        }
        this.lastSelectionSlot = 0.coerceAtLeast(slot - 1)

        inventory.setItem(size - 1, this.next)
        for (i in  size - 2 downTo size - 8) {
            inventory.setItem(i, this.filler)
        }
        inventory.setItem(size - 9, this.previous)
        inventory.setItem(size - 5, this.back)
    }

    override fun onClick(slotId: Int, button: Int, type: ClickType, player: ServerPlayer) {
        val size = this.getContainer().containerSize
        val next = size - 1
        val back = size - 5
        val previous = size - 9
        if (slotId in previous..next) {
            if (slotId == next) {
                player.openMenu(createScreenFactory(this, this.page + 1))
            } else if (slotId == previous) {
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

    override fun onTick(server: MinecraftServer) {
        val container = this.getContainer()
        for (slot in 0..this.lastSelectionSlot) {
            val stack = container.getItem(slot)
            this.tickers.forEach { ticker -> ticker.tick(stack) }
        }
    }

    internal class Selection(
        val display: ItemStack,
        val action: (ServerPlayer) -> Unit
    )

    companion object {
        internal fun createScreenFactory(previous: SelectionScreen, page: Int): SimpleMenuProvider? {
            return createScreenFactory(
                previous.title,
                previous.selections,
                previous.tickers,
                previous.parent,
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
            page: Int,
            previous: ItemStack,
            back: ItemStack,
            next: ItemStack,
            filler: ItemStack,
        ): SimpleMenuProvider? {
            if (page >= 0) {
                return SimpleMenuProvider(
                    { syncId, _, player ->
                        SelectionScreen(title, selections, tickers, player, syncId, parent, page, previous, back, next, filler)
                    },
                    title
                )
            }
            return null
        }
    }
}