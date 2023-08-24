package net.casualuhc.arcade.gui.screen

import net.casualuhc.arcade.Arcade
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack

class SelectionScreen internal constructor(
    private val title: Component,
    private val selections: List<Selection>,
    player: Player,
    syncId: Int,
    private val page: Int,
    private val previous: ItemStack,
    private val next: ItemStack,
    private val filler: ItemStack
): InterfaceScreen(player, syncId, 6) {
    init {
        val inventory = this.getContainer()
        val size = inventory.containerSize
        val selectionSize = size - 9

        val paged = this.selections.stream()
            .skip((selectionSize * page).toLong())
            .limit(selectionSize.toLong())
            .toList()

        for ((i, selection) in paged.withIndex()) {
            inventory.setItem(i, selection.display)
        }

        inventory.setItem(size - 1, this.next)
        for (i in  size - 2 downTo size - 8) {
            inventory.setItem(i, this.filler)
        }
        inventory.setItem(size - 9, this.previous)
    }

    override fun onClick(slotId: Int, button: Int, type: ClickType, player: ServerPlayer) {
        val size = this.getContainer().containerSize
        val next = size - 1
        val previous = size - 9
        if (slotId in previous..next) {
            if (slotId == next) {
                player.openMenu(createScreenFactory(this, this.page + 1))
            } else if (slotId == previous) {
                player.openMenu(createScreenFactory(this, this.page - 1))
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

    internal class Selection(
        val display: ItemStack,
        val action: (ServerPlayer) -> Unit
    )

    companion object {
        internal fun createScreenFactory(previous: SelectionScreen, page: Int): SimpleMenuProvider? {
            return createScreenFactory(
                previous.title,
                previous.selections,
                page,
                previous.previous,
                previous.next,
                previous.filler
            )
        }

        internal fun createScreenFactory(
            title: Component,
            selections: List<Selection>,
            page: Int,
            previous: ItemStack,
            next: ItemStack,
            filler: ItemStack
        ): SimpleMenuProvider? {
            if (page >= 0) {
                return SimpleMenuProvider(
                    { syncId, inv, player ->
                        SelectionScreen(title, selections, player, syncId, page, previous, next, filler)
                    },
                    title
                )
            }
            return null
        }
    }
}