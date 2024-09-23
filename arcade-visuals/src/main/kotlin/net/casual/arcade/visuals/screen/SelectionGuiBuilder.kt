package net.casual.arcade.visuals.screen

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementInterface
import eu.pb4.sgui.api.gui.GuiInterface
import eu.pb4.sgui.api.gui.SimpleGui
import eu.pb4.sgui.api.gui.SimpleGuiBuilder
import eu.pb4.sgui.api.gui.SlotGuiInterface
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.math.min
import net.minecraft.world.inventory.ClickType as ActionType

/**
 * This class allows you to build your own gui's with selectable elements.
 *
 * @param player The player that will be opening this gui.
 * @param components The default components used for the selection gui.
 */
public class SelectionGuiBuilder(
    private val player: ServerPlayer,
    components: SelectionGuiComponents = SelectionGuiComponents.DEFAULT
) {
    private val components = SelectionGuiComponents.Builder(components)
    private val elements = ArrayList<GuiElementInterface>()
    private val menuElements = EnumMap<MenuSlot, GuiElementInterface>(MenuSlot::class.java)

    /**
     * The parent [GuiInterface], may be null.
     */
    private var parent: GuiInterface? = null

    /**
     * The style of the selection gui.
     */
    public var style: SelectionGuiStyle = SelectionGuiStyle.DEFAULT

    /**
     * This constructs this gui builder with a parent. The owner
     * of the parent will be used as the owner of this gui.]
     *
     * @param parent The parent of this gui.
     * @param components The default components used for the selection gui.
     */
    public constructor(
        parent: GuiInterface,
        components: SelectionGuiComponents = SelectionGuiComponents.DEFAULT
    ): this(parent.player, components) {
        this.parent = parent
    }

    /**
     * This sets the parent gui, it must have belong to the same
     * player as this gui.
     *
     * @param parent The parent gui.
     * @return The current [SelectionGuiBuilder].
     */
    public fun parent(parent: GuiInterface): SelectionGuiBuilder {
        if (this.player != parent.player) {
            throw IllegalArgumentException("Parent GUI must belong to the same player!")
        }
        this.parent = parent
        return this
    }

    /**
     * Lets you build the menu components of the selection screen.
     *
     * @param block The builder block.
     * @return The current [SelectionGuiBuilder].
     */
    public fun components(block: SelectionGuiComponents.Builder.() -> Unit): SelectionGuiBuilder {
        block(this.components)
        return this
    }

    /**
     * Sets the style of the gui.
     *
     * @param style The style of the screen.
     * @return The current [SelectionGuiBuilder].
     */
    public fun style(style: SelectionGuiStyle): SelectionGuiBuilder {
        this.style = style
        return this
    }

    /**
     * This adds a selectable [ItemStack] that has an action when
     * the player clicks on the item.
     *
     * @param element The selection.
     * @return The current [SelectionGuiBuilder].
     */
    public fun element(element: GuiElementInterface): SelectionGuiBuilder {
        this.elements.add(element)
        return this
    }

    /**
     * This adds elements that can be mapped to item stacks, which
     * can are then provided in the callback.
     *
     * @param elements The elements to add.
     * @param elementToIconMapper The element to [ItemStack] mapper.
     * @param callback The callback for the elements.
     * @return The current [SelectionGuiBuilder].
     */
    public fun <T> elements(
        elements: Iterable<T>,
        elementToIconMapper: (T) -> ItemStack,
        callback: (slot: Int, click: ClickType, action: ActionType, gui: SlotGuiInterface, T) -> Unit
    ): SelectionGuiBuilder {
        for (element in elements) {
            this.element(GuiElement(elementToIconMapper.invoke(element)) { slot, click, action, gui ->
                callback.invoke(slot, click, action, gui, element)
            })
        }
        return this
    }

    /**
     * Sets an additional button to the selection
     * screen on the menu bar.
     *
     * @param slot The slot to add it to.
     * @param selection The selection.
     * @return The current [SelectionGuiBuilder].
     */
    public fun menuElement(
        slot: MenuSlot,
        selection: GuiElementInterface
    ): SelectionGuiBuilder {
        this.menuElements[slot] = selection
        return this
    }

    /**
     * This builds the [SelectionGuiBuilder] into a [SimpleGui]
     * you can then simply call [SimpleGui.open] to open the gui.
     *
     * @return The built [SimpleGui].
     */
    public fun build(page: Int = 0): SimpleGui {
        val builder = SimpleGuiBuilder(MenuType.GENERIC_9x6, false)
        builder.title = this.components.title
        val slots = this.style.getSlots()
        val count = min(builder.size, slots.size)

        val paged = this.elements.stream()
            .skip((count * page).toLong())
            .limit(count.toLong())
            .toList()

        val hasNextPage = this.elements.size > count * (page + 1)
        for ((i, slot) in slots.withIndex()) {
            if (i >= paged.size || slot !in 0..< builder.size) {
                break
            }
            val element = paged[i]
            builder.setSlot(slot, element)
        }

        builder.setSlot(45, GuiElement(this.components.getPrevious(page != 0)) { _, _, _, _ ->
            if (page > 0) {
                this.build(page - 1).open()
            }
        })
        builder.setSlot(49, GuiElement(this.components.getBack(this.parent != null)) { _, _, _, gui ->
            val parent = this.parent
            if (parent != null) {
                parent.open()
            } else {
                gui.close()
            }
        })
        builder.setSlot(53, GuiElement(this.components.getNext(hasNextPage)) { _, _, _, _ ->
            if (hasNextPage) {
                this.build(page + 1).open()
            }
        })

        for (slot in MenuSlot.entries) {
            val element = this.menuElements.getOrElse(slot) {
                GuiElement(this.components.getFiller(), GuiElementInterface.EMPTY_CALLBACK)
            }
            builder.setSlot(45 + slot.offset, element)
        }
        for ((slot, element) in this.menuElements) {
             builder.setSlot(45 + slot.offset, element)
        }
        return builder.build(this.player)
    }

    public enum class MenuSlot(public val offset: Int) {
        FIRST(1),
        SECOND(2),
        THIRD(3),
        FOURTH(5),
        FIFTH(6),
        SIXTH(7);
    }
}