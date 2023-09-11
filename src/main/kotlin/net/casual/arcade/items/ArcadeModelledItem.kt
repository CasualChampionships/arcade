package net.casual.arcade.items

import eu.pb4.polymer.core.api.item.PolymerItem
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.jetbrains.annotations.ApiStatus.NonExtendable

/**
 * This interface allows you to create your own
 * custom modelled items using [PolymerItem].
 *
 * ```kotlin
 * // Your creator for your resource pack
 * // See polymer for more information...
 * val creator = ResourcePackCreator.create()
 *
 * class MyItem private constructor(): Item(Properties()), ArcadeModelledItem {
 *     override fun getPolymerItem(stack: ItemStack, player: ServerPlayer?): Item {
 *         // Whatever item you want to shadow...
 *         return Items.POPPED_CHORUS_FRUIT
 *     }
 *
 *     override fun getModeller(): ItemModeller {
 *         return MODELLER
 *     }
 *
 *     companion object {
 *         val MODELLER = ResourcePackItemModeller(MyItem(), creator)
 *         val FOO = MODELLER.model(ResourceLocation("modid", "foo"))
 *         val BAR = MODELLER.model(ResourceLocation("modid", "bar"))
 *     }
 * }
 *
 * object MyItems {
 *     val MY_ITEM = register("my_item", MyItem.MODELLER.item())
 *
 *     // This method must be called in your ModInitializer
 *     fun noop() {
 *
 *     }
 *
 *     private fun <T: Item> register(path: String, item: T): T {
 *         Items.registerItem(ResourceLocation("modid", path), item)
 *         return item
 *     }
 * }
 * ```
 *
 * Then to create your custom modelled item stacks, you can simply do this:
 *
 * ```kotlin
 * fun foobar() {
 *     val foo = MyItem.FOO.create()
 *     val bar = MyItem.BAR.create()
 * }
 * ```
 */
interface ArcadeModelledItem: PolymerItem {
    /**
     * Returns main/default item used on the client for specific player.
     *
     * @param stack ItemStack of virtual item.
     * @param player The player for which it's sent to, may be null.
     * @return Vanilla (or other) item instance.
     * @see PolymerItem
     */
    override fun getPolymerItem(stack: ItemStack, player: ServerPlayer?): Item

    /**
     * This gets all the possible states for this item
     * which is managed by a [ItemModeller].
     *
     * @return The modelled item handler.
     * @see ItemModeller
     */
    fun getModeller(): ItemModeller

    /**
     * Method used for getting custom model data of items.
     *
     * @param stack ItemStack of virtual item.
     * @param player The player for which it's sent to.
     * @return Custom model data or -1 if not present.
     */
    @NonExtendable
    override fun getPolymerCustomModelData(stack: ItemStack, player: ServerPlayer?): Int {
        return this.getModeller().getModelId(stack)
    }
}