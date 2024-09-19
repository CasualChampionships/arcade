package net.casual.arcade.dimensions

import net.casual.arcade.commands.register
import net.casual.arcade.dimensions.border.WorldBorderCommandModifier
import net.casual.arcade.dimensions.border.extensions.BorderSerializerExtension
import net.casual.arcade.dimensions.utils.FantasyUtils
import net.casual.arcade.dimensions.vanilla.DragonDataExtension
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.fabricmc.api.ModInitializer

public object ArcadeDimensions: ModInitializer {
    override fun onInitialize() {
        DragonDataExtension.registerEvents()
        BorderSerializerExtension.registerEvents()
        FantasyUtils.registerEvents()

        GlobalEventHandler.register<ServerRegisterCommandEvent> { event ->
            event.register(WorldBorderCommandModifier)
        }
    }
}