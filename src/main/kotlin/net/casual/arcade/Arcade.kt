package net.casual.arcade

import net.fabricmc.api.ModInitializer
import net.minecraft.resources.ResourceLocation

/**
 * Arcade initializer class.
 */
public object Arcade: ModInitializer {
    /**
     * The mod identifier for Arcade.
     */
    public const val MOD_ID: String = "arcade"

    /**
     * Creates a [ResourceLocation] with the namespace of [MOD_ID].
     *
     * @param path The path of the [ResourceLocation].
     * @return The created [ResourceLocation].
     */
    @JvmStatic
    public fun id(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
    }

    override fun onInitialize() {

    }
}
