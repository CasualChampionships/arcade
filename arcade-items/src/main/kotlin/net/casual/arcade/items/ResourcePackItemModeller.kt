package net.casual.arcade.items

import com.mojang.serialization.Codec
import eu.pb4.polymer.core.api.other.PolymerComponent
import eu.pb4.polymer.resourcepack.api.PolymerModelData
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.items.ItemModeller.Companion.registerNextModel
import net.casual.arcade.utils.ItemUtils.isOf
import net.casual.arcade.utils.ResourceUtils
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.codec.ByteBufCodecs
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
    private val states = Object2ObjectLinkedOpenHashMap<ResourceLocation, ItemStack.() -> Unit>()

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
     * Gets a list of all the different item models.
     *
     * @return All the different models.
     */
    public fun all(): List<ItemStack> {
        return this.states.keys.map(this::create)
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
        this.states[location] = modifier
        this.creator.registerNextModel(this.client, location)
        return ItemStackFactory {
            this.create(location)
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
        val modifier = this.states[location]
            ?: throw IllegalArgumentException("No state '$location' has been defined")
        stack.set(PACKED_CUSTOM_MODEL, this.getModelData(location).value())
        stack.modifier()
        return stack
    }

    /**
     * This gets the custom model id of a given
     * [ItemStack] to send to the client.
     *
     * @param stack The stack to get the id of.
     * @return The custom model id.
     */
    override fun getModelId(stack: ItemStack): Int {
        if (!stack.isOf(this.item())) {
            throw IllegalArgumentException("Cannot get model ID for incorrect stack '${stack}'")
        }
        return stack.get(PACKED_CUSTOM_MODEL) ?: -1
    }

    private fun getModelData(id: ResourceLocation): PolymerModelData {
        return this.creator.requestModel(this.client, id)
    }

    private companion object {
        private val PACKED_CUSTOM_MODEL = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceUtils.arcade("packed_custom_model"),
            DataComponentType.builder<Int>().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build()
        )

        init {
            PolymerComponent.registerDataComponent(PACKED_CUSTOM_MODEL)
        }
    }
}