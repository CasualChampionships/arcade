package net.casual.arcade.datagen.resource

import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation

public interface ArcadeResourceGenerator {
    public fun id(): String

    public fun run(client: Minecraft)

    public fun resources(): Collection<ResourcePackCreator>
}