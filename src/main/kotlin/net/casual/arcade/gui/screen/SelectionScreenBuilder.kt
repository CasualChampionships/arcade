package net.casual.arcade.gui.screen

import net.casual.arcade.gui.screen.SelectionScreen.Selection
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.collections.ArrayList

/**
 * This class allows you to build your own [SelectionScreen]s.
 *
 * A selection screen allows you to add items that when clicked
 * will run an action.
 * You can also add an [ItemStackTicker] to update any items that
 * are currently on the screen.
 *
 * @param components The default [SelectionScreenComponents].
 * @see SelectionScreen
 */
public class SelectionScreenBuilder(
    components: SelectionScreenComponents = SelectionScreenComponents.DEFAULT
) {
    private val selections = ArrayList<Selection>()
    private val tickers = ArrayList<ItemStackTicker>()
    private val components = SelectionScreenComponents.Builder(components)
    private val buttons = EnumMap<SelectionScreen.Slot, Selection>(SelectionScreen.Slot::class.java)

    /**
     * The parent [MenuProvider], may be null.
     */
    public var parent: MenuProvider? = null

    /**
     * The style of the selection screen.
     */
    public var style: SelectionScreenStyle = SelectionScreenStyle.DEFAULT

    /**
     * Sets the parent screen of the [SelectionScreen].
     *
     * @param provider The parent screen provider.
     * @return The current [SelectionScreenBuilder].
     */
    public fun parent(provider: MenuProvider?): SelectionScreenBuilder {
        this.parent = provider
        return this
    }

    /**
     * Lets you build the menu components of the selection screen.
     *
     * @param block The builder block.
     * @return The current [SelectionScreenBuilder].
     */
    public fun components(block: SelectionScreenComponents.Builder.() -> Unit): SelectionScreenBuilder {
        block(this.components)
        return this
    }

    /**
     * Sets the style of the [SelectionScreen].
     *
     * @param style The style of the screen.
     * @return The current [SelectionScreenBuilder].
     */
    public fun style(style: SelectionScreenStyle): SelectionScreenBuilder {
        this.style = style
        return this
    }

    /**
     * This adds a selectable [ItemStack] that has an [action] when
     * the player clicks on the item.
     *
     * @param stack The clickable [ItemStack].
     * @param action The action to run when [stack] is clicked.
     * @return The current [SelectionScreenBuilder].
     */
    public fun selection(stack: ItemStack, action: (ServerPlayer) -> Unit): SelectionScreenBuilder {
        this.selections.add(Selection(stack.copy(), action))
        return this
    }

    /**
     * Sets an additional button to the selection
     * screen on the menu bar.
     *
     * @param slot The slot to add it to.
     * @param stack The display stack.
     * @param action The action to run when clicking the button.
     */
    public fun button(
        slot: SelectionScreen.Slot,
        stack: ItemStack,
        action: (ServerPlayer) -> Unit
    ): SelectionScreenBuilder {
        this.buttons[slot] = Selection(stack, action)
        return this
    }

    /**
     * This adds a [ticker] to the [SelectionScreenBuilder] which
     * can update any of the selectable [ItemStack].
     *
     * @param ticker The [ItemStackTicker].
     * @return The current [SelectionScreenBuilder].
     */
    public fun ticker(ticker: ItemStackTicker): SelectionScreenBuilder {
        this.tickers.add(ticker)
        return this
    }

    /**
     * This builds the [SelectionScreenBuilder] into a [MenuProvider]
     * which can be passed into [ServerPlayer.openMenu] to open the
     * screen.
     *
     * @return The built [MenuProvider].
     */
    public fun build(): MenuProvider {
        return SelectionScreen.createScreenFactory(
            this.components.build(),
            this.selections,
            this.tickers,
            this.parent,
            this.style,
            this.buttons,
            0
        )!!
    }
}