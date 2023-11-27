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
    public fun getPrevious(): ItemStack {
        return ItemStack(Items.RED_STAINED_GLASS).named("Previous")
    }

    /**
     * Gets the back [ItemStack] instance.
     *
     * @return The default back [ItemStack]
     */
    public fun getBack(): ItemStack {
        return ItemStack(Items.RED_STAINED_GLASS).named("Back")
    }

    /**
     * Gets the next [ItemStack] instance.
     *
     * @return The default next [ItemStack]
     */
    public fun getNext(): ItemStack {
        return ItemStack(Items.GREEN_STAINED_GLASS).named("Next")
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
}