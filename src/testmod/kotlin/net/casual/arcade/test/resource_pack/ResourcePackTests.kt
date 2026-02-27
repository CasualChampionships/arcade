package net.casual.arcade.test.resource_pack

import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.utils.ResourcePackUtils.addFont
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import kotlin.io.path.createParentDirectories

object ResourcePackTests {
    val CREATOR = NamedResourcePackCreator.named("testing") {
        addFont(CustomFontResources)
        packDescription = Component.literal("Testing resource pack")
    }

    fun run() {
        val output = FabricLoader.getInstance().configDir.resolve("generated-rps")
        output.createParentDirectories()
        CREATOR.buildTo(output)
    }
}