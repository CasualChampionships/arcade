package net.casual.arcade.gui.screen

import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ItemUtils.named
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

/**
 * This interface provides methods for getting default components
 * for a [SelectionScreenBuilder].
 *
 * @see SelectionScreenBuilder
 */
public interface SelectionScreenComponents {
    /**
     * Gets the default title.
     *
     * @return The default title [Component].
     */
    public fun getTitle(): Component {
        return "Selection Screen".literal()
    }

    /**
     * Gets the previous [ItemStack] instance.
     *
     * @return The default previous [ItemStack]
     */
    public fun getPrevious(hasPrevious: Boolean): ItemStack {
        val item = if (hasPrevious) Items.RED_STAINED_GLASS else Items.GRAY_STAINED_GLASS
        return ItemStack(item).named("Previous")
    }

    /**
     * Gets the back [ItemStack] instance.
     *
     * @return The default back [ItemStack]
     */
    public fun getBack(hasParent: Boolean): ItemStack {
        return ItemStack(Items.RED_STAINED_GLASS).named(if (hasParent) "Back" else "Exit")
    }

    /**
     * Gets the next [ItemStack] instance.
     *
     * @return The default next [ItemStack]
     */
    public fun getNext(hasNext: Boolean): ItemStack {
        val item = if (hasNext) Items.GREEN_STAINED_GLASS else Items.GRAY_STAINED_GLASS
        return ItemStack(item).named("Next")
    }

    /**
     * Gets the filler [ItemStack] instance.
     *
     * @return The default filler [ItemStack]
     */
    public fun getFiller(): ItemStack {
        return ItemStack(Items.GRAY_STAINED_GLASS).named("")
    }

    public companion object {
        /**
         * The default implementation of the [SelectionScreenComponents].
         */
        public val DEFAULT: SelectionScreenComponents = object: SelectionScreenComponents { }
    }

    public class Builder(defaults: SelectionScreenComponents = DEFAULT) {
        public var title: Component = defaults.getTitle()

        public var previous: ItemStack = defaults.getPrevious(true)
        public var noPrevious: ItemStack = defaults.getPrevious(false)

        public var next: ItemStack = defaults.getNext(true)
        public var noNext: ItemStack = defaults.getNext(false)

        public var back: ItemStack = defaults.getBack(true)
        public var exit: ItemStack = defaults.getBack(false)

        public var filler: ItemStack = defaults.getFiller()

        /**
         * Sets the [title] [Component].
         *
         * @param component The title.
         * @return The current [Builder].
         */
        public fun title(component: Component): Builder {
            this.title = component
            return this
        }

        /**
         * Sets the [next] [ItemStack] instance.
         *
         * @param present The stack for when there is a previous button.
         * @param absent The stack for when there is not a previous button.
         * @return The current [Builder].
         */
        public fun previous(present: ItemStack, absent: ItemStack = present): Builder {
            this.previous = present
            this.noPrevious = absent
            return this
        }

        /**
         * Sets the [next] [ItemStack] instance.
         *
         * @param present The stack for when there is a back button.
         * @param absent The stack for when there is not a back button.
         * @return The current [Builder].
         */
        public fun back(present: ItemStack, absent: ItemStack = present): Builder {
            this.back = present
            this.exit = absent
            return this
        }

        /**
         * Sets the [next] [ItemStack] instance.
         *
         * @param present The stack for when there is a next button.
         * @param absent The stack for when there is not a next button.
         * @return The current [Builder].
         */
        public fun next(present: ItemStack, absent: ItemStack = present): Builder {
            this.next = present
            this.noNext = absent
            return this
        }

        /**
         * Sets the [filler] [ItemStack] instance.
         *
         * @param stack The filler item.
         * @return The current [Builder].
         */
        public fun filler(stack: ItemStack): Builder {
            this.filler = stack
            return this
        }

        public fun build(): SelectionScreenComponents {
            return object: SelectionScreenComponents {
                override fun getTitle(): Component {
                    return title
                }

                override fun getPrevious(hasPrevious: Boolean): ItemStack {
                    return if (hasPrevious) previous else noPrevious
                }

                override fun getBack(hasParent: Boolean): ItemStack {
                    return if (hasParent) back else exit
                }

                override fun getNext(hasNext: Boolean): ItemStack {
                    return if (hasNext) next else noNext
                }

                override fun getFiller(): ItemStack {
                    return filler
                }
            }
        }
    }
}