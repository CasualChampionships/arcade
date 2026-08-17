package net.casual.arcade.tests.manual.resource_pack

import net.casual.arcade.pack.generation.PackDefinition
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component

object ResourcePackTests {
    val PACK = PackDefinition("testing") {
        description = Component.literal("Testing resource pack")
        addFont(CustomFontResources)
    }

    fun run() {
        PACK.buildTo(FabricLoader.getInstance().configDir.resolve("generated-rps"))
    }
}
