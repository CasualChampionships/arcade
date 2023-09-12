package net.casual.arcade.gui.screen

import net.casual.arcade.gui.screen.SelectionScreen.Selection
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.item.ItemStack

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

    /**
     * The title of the selection screen.
     */
    public var title: Component = components.getTitle()

    /**
     * The previous [ItemStack] instance.
     */
    public var previous: ItemStack = components.getPrevious()

    /**
     * The back [ItemStack] instance.
     */
    public var back: ItemStack = components.getBack()

    /**
     * The next [ItemStack] instance.
     */
    public var next: ItemStack = components.getNext()

    /**
     * The filler [ItemStack] instance.
     */
    public var filler: ItemStack = components.getFiller()

    /**
     * The parent [MenuProvider], may be null.
     */
    public var parent: MenuProvider? = null

    /**
     * Sets the [title] [Component].
     *
     * @param component The title.
     * @return The current [SelectionScreenBuilder].
     */
    public fun title(component: Component): SelectionScreenBuilder {
        this.title = component
        return this
    }

    /**
     * Sets the [previous] [ItemStack] instance.
     *
     * @param stack The title.
     * @return The current [SelectionScreenBuilder].
     */
    public fun previous(stack: ItemStack): SelectionScreenBuilder {
        this.previous = stack
        return this
    }

    /**
     * Sets the [back] [ItemStack] instance.
     *
     * @param stack The title.
     * @return The current [SelectionScreenBuilder].
     */
    public fun back(stack: ItemStack): SelectionScreenBuilder {
        this.back = stack
        return this
    }

    /**
     * Sets the [next] [ItemStack] instance.
     *
     * @param stack The title.
     * @return The current [SelectionScreenBuilder].
     */
    public fun next(stack: ItemStack): SelectionScreenBuilder {
        this.next = stack
        return this
    }

    /**
     * Sets the [filler] [ItemStack] instance.
     *
     * @param stack The title.
     * @return The current [SelectionScreenBuilder].
     */
    public fun filler(stack: ItemStack): SelectionScreenBuilder {
        this.filler = stack
        return this
    }

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
            this.title,
            this.selections,
            this.tickers,
            this.parent,
            0,
            this.previous,
            this.back,
            this.next,
            this.filler
        )!!
    }
}