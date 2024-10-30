package net.casual.arcade.test

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry

object ArcadeTest: ModInitializer {
    override fun onInitialize() {
        Registry.register(
            MinigameRegistries.MINIGAME_FACTORY,
            TestMinigame.ID,
            TestMinigame.codec()
        )
    }
}

object Example: ModInitializer {
    override fun onInitialize() = GlobalEventHandler.register<ServerLoadedEvent> { (server) ->

    }
}