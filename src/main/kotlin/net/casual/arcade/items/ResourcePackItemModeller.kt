package net.casual.arcade.items

import eu.pb4.polymer.core.api.item.PolymerItemUtils
import eu.pb4.polymer.resourcepack.api.PolymerModelData
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.casual.arcade.Arcade
import net.casual.arcade.utils.ItemUtils.putIntElement
import net.casual.arcade.utils.ResourcePackUtils.registerNextModel
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/**
 * This is an implementation of [ItemModeller] that allows
 * you to create custom item models using a [ResourcePackCreator].
 *
 * **An important thing to note:**
 * If you have multiple [ArcadeModelledItem]s with different [ResourcePackCreator]s
 * you must either make sure that you use different base items or merge all items
 * that use the same base item into one [ResourcePackCreator].
 *
 * This is because Minecraft only allows for one override per item, so if you
 * have multiple packs with model data, one will simply take precedence.
 *
 * See [ArcadeModelledItem] for an example on how to create
 * custom-modelled items using this class.
 *
 * @param modelledItem The modelled item instance.
 * @param creator The resource pack creator.
 * @see ArcadeModelledItem
 * @see ResourcePackCreator
 */
public class ResourcePackItemModeller(
    modelledItem: ArcadeModelledItem,
    private val creator: ResourcePackCreator
): ItemModeller {
    private val states = ArrayList<ResourceLocation>()

    private val server: Item
    private val client: Item

    init {
        if (modelledItem !is Item) {
            throw IllegalArgumentException("ArcadeModelledItem implementation must extend Item!")
        }
        this.server = modelledItem
        this.client = modelledItem.getPolymerReplacement(null)
    }

    /**
     * This gets the server-sided item.
     *
     * @return The server sided item.
     */
    public fun item(): Item {
        return this.server
    }

     /**
      * This registers a custom model for a given [location].
      *
      * @param location The location of the custom model in the
      * given resource pack creator.
      * @param modifier A function that modifies the stack.
      * @return The [ItemStack] generator.
      */
    public fun model(
         location: ResourceLocation,
         modifier: ItemStack.() -> Unit = {}
    ): ItemStackFactory {
        this.states.add(location)
        this.creator.registerNextModel(this.client, location)
        return ItemStackFactory {
            val stack = this.create(location)
            stack.modifier()
            stack
        }
    }

    /**
     * This creates a modelled version of [item]
     * using the given [location].
     *
     * This may throw [IllegalArgumentException] if the
     * location of the modelled item is not registered
     * or doesn't exist.
     *
     * @param location The location of the modelled item.
     * @return The modelled [ItemStack].
     */
    public fun create(location: ResourceLocation): ItemStack {
        val stack = ItemStack(this.item())
        stack.set(PACKED_CUSTOM_MODEL, this.getModelData(location).value())
        return stack
    }

    /**
     * This creates a modelled version of [item]
     * using the given [id].
     *
     * This may throw [IllegalArgumentException] if the
     * index of the modelled item is out of bounds.
     *
     * @param id The state id.
     * @return The modelled [ItemStack].
     */
    public fun create(id: Int): ItemStack {
        return this.create(this.getLocation(id))
    }

    /**
     * This gets the custom model id of a given
     * [ItemStack] to send to the client.
     *
     * @param stack The stack to get the id of.
     * @return The custom model id.
     */
    override fun getModelId(stack: ItemStack): Int {
        if (!stack.`is`(this.item())) {
            throw IllegalArgumentException("Cannot get model ID for incorrect stack '${stack}'")
        }
        return stack.get(PACKED_CUSTOM_MODEL) ?: -1
    }

    private fun getLocation(state: Int): ResourceLocation {
        return this.states[state]
    }

    private fun getModelData(id: ResourceLocation): PolymerModelData {
        if (!this.states.contains(id)) {
            throw IllegalArgumentException("No state '$id' has been defined")
        }
        return this.creator.requestModel(this.client, id)
    }

    private companion object {
        private const val ID = "arcade_packed_custom_model"

        private val PACKED_CUSTOM_MODEL = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Arcade.id("packed_custom_model"),
            DataComponentType.builder<Int>().build()
        )

        init {
            PolymerItemUtils.markAsPolymer(PACKED_CUSTOM_MODEL)
        }
    }
}