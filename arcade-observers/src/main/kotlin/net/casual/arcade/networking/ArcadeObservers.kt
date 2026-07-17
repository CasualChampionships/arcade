package net.casual.arcade.networking

import net.casual.arcade.networking.extensions.PlayerObserverExtension
import net.fabricmc.api.ModInitializer

public object ArcadeObservers: ModInitializer {
    public const val MOD_ID: String = "arcade-observers"

    override fun onInitialize() {
        PlayerObserverExtension.registerEvents()
    }
}