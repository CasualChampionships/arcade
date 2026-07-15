package net.casual.arcade.networking

import net.casual.arcade.networking.extensions.PlayerObserverExtension
import net.fabricmc.api.ModInitializer

public object ArcadeNetworking: ModInitializer {
    override fun onInitialize() {
        PlayerObserverExtension.registerEvents()
    }
}