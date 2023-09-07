package net.casual.arcade.gui.screen

import net.casual.arcade.gui.screen.SelectionScreen.Selection
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.item.ItemStack

class SelectionScreenBuilder(
    components: SelectionScreenComponents = SelectionScreenComponents.DEFAULT
) {
    private val selections = ArrayList<Selection>()
    private val tickers = ArrayList<ItemStackTicker>()

    var title = components.getTitle()
    var previous = components.getPrevious()
    var back = components.getBack()
    var next = components.getNext()
    var filler = components.getFiller()
    var parent: MenuProvider? = null

    fun title(component: Component): SelectionScreenBuilder {
        this.title = component
        return this
    }

    fun previous(stack: ItemStack): SelectionScreenBuilder {
        this.previous = stack
        return this
    }

    fun back(stack: ItemStack): SelectionScreenBuilder {
        this.back = stack
        return this
    }

    fun next(stack: ItemStack): SelectionScreenBuilder {
        this.next = stack
        return this
    }

    fun filler(stack: ItemStack): SelectionScreenBuilder {
        this.filler = stack
        return this
    }

    fun parent(provider: MenuProvider?): SelectionScreenBuilder {
        this.parent = provider
        return this
    }

    fun selection(stack: ItemStack, action: (ServerPlayer) -> Unit): SelectionScreenBuilder {
        this.selections.add(Selection(stack.copy(), action))
        return this
    }

    fun ticker(ticker: ItemStackTicker): SelectionScreenBuilder {
        this.tickers.add(ticker)
        return this
    }

    fun build(): MenuProvider {
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