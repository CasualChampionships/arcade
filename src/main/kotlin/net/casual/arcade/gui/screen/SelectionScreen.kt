package net.casual.arcade.gui.screen

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickType
import java.util.*

/**
 * This [ArcadeGenericScreen] implementation is a customizable
 * screen that allows you to create functional, clickable, items.
 *
 * The menu provides options for returning to a parent screen as
 * well as setting custom items for the back, next, and filler items.
 *
 * You can create your own selection screens using [SelectionScreenBuilder].
 *
 * @param selections The possible selections.
 * @param player The player opening the screen.
 * @param syncId The sync id.
 * @param parent The parent menu.
 * @param page The page of the screen.
 * @see SelectionScreenBuilder
 * @see ArcadeGenericScreen
 */
public class SelectionScreen internal constructor(
    private val components: SelectionScreenComponents,
    private val selections: List<SelectableMenuItem>,
    player: Player,
    syncId: Int,
    private val parent: MenuProvider?,
    private val style: SelectionScreenStyle,
    private val buttons: EnumMap<Slot, SelectableMenuItem>,
    private val page: Int
): ArcadeGenericScreen(player, syncId, 6) {
    private val pageSelections = Int2ObjectOpenHashMap<SelectableMenuItem>()
    private val player: ServerPlayer = player as ServerPlayer
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
            if (i >= paged.size) {
                break
            }
            val selection = paged[i]
            this.pageSelections[slot] = selection
            inventory.setItem(slot, selection.default)
        }

        inventory.setItem(45, this.components.getPrevious(this.page != 0))
        inventory.setItem(49, this.components.getBack(this.parent != null))
        inventory.setItem(53, this.components.getNext(this.hasNextPage))

        for (slot in Slot.entries) {
            val index = slot.offsetFrom(45)
            val selection = this.buttons[slot]
            if (selection == null) {
                inventory.setItem(index, this.components.getFiller())
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
            val slot = Slot.fromOffset(slotId - 45) ?: return
            val selection = this.buttons[slot] ?: return
            selection.selected(player)
            return
        }

        this.pageSelections[slotId]?.selected(player)
    }

    /**
     * This method is called every server tick.
     *
     * @param server The [MinecraftServer] instance.
     */
    override fun onTick(server: MinecraftServer) {
        val container = this.getContainer()
        for ((slot, selection) in this.pageSelections) {
            if (selection.shouldUpdate(this.player)) {
                val previous = container.getItem(slot)
                val updated = selection.update(previous, this.player)
                if (updated !== previous) {
                    container.setItem(slot, updated)
                }
            }
        }
    }

    public enum class Slot(private val offset: Int) {
        FIRST(1),
        SECOND(2),
        THIRD(3),
        FOURTH(5),
        FIFTH(6),
        SIXTH(7);

        internal fun offsetFrom(index: Int): Int {
            return index + this.offset
        }

        internal companion object {
            fun fromOffset(offset: Int): Slot? {
                val shifted = if (offset >= 5) offset - 2 else offset - 1
                val slots = entries
                if (shifted !in slots.indices) {
                    return null
                }
                return slots[shifted]
            }
        }
    }

    public companion object {
        internal fun createScreenFactory(previous: SelectionScreen, page: Int): SimpleMenuProvider? {
            return createScreenFactory(
                previous.components,
                previous.selections,
                previous.parent,
                previous.style,
                previous.buttons,
                page
            )
        }

        internal fun createScreenFactory(
            components: SelectionScreenComponents,
            selections: List<SelectableMenuItem>,
            parent: MenuProvider?,
            style: SelectionScreenStyle,
            buttons: EnumMap<Slot, SelectableMenuItem>,
            page: Int,
        ): SimpleMenuProvider? {
            if (page >= 0) {
                return SimpleMenuProvider(
                    { syncId, _, player ->
                        SelectionScreen(components, selections, player, syncId, parent, style, buttons, page)
                    },
                    components.getTitle()
                )
            }
            return null
        }
    }
}