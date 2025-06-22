/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border

import net.casual.arcade.border.command.WorldBorderCommandModifier
import net.casual.arcade.border.extensions.BorderSerializerExtension
import net.casual.arcade.border.extensions.PlayerWorldBorderExtension
import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.fabricmc.api.ModInitializer
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
public object ArcadeBorder: ModInitializer {
    override fun onInitialize() {
        BorderSerializerExtension.registerEvents()
        PlayerWorldBorderExtension.registerEvents()

        GlobalEventHandler.Server.register<ServerRegisterCommandEvent> { event ->
            event.register(WorldBorderCommandModifier)
        }
    }
}