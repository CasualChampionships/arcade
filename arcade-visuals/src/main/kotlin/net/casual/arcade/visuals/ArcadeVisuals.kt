package net.casual.arcade.visuals

import net.casual.arcade.visuals.extensions.PlayerBossbarsExtension
import net.casual.arcade.visuals.extensions.PlayerSidebarExtension
import net.casual.arcade.visuals.extensions.PlayerTabDisplayExtension
import net.fabricmc.api.ModInitializer

public object ArcadeVisuals: ModInitializer {
    override fun onInitialize() {
        PlayerSidebarExtension.registerEvents()
        PlayerTabDisplayExtension.registerEvents()
        PlayerBossbarsExtension.registerEvents()
    }
}