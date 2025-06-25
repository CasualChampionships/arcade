package net.casual.arcade.test

import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.test.command.LevelBoundaryCommand
import net.casual.arcade.test.minigame.TestMinigame
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry

object ArcadeTest: ModInitializer {
    override fun onInitialize() {
        GlobalEventHandler.Server.register<ServerRegisterCommandEvent> {
            it.register(LevelBoundaryCommand)
        }

        Registry.register(
            MinigameRegistries.MINIGAME_FACTORY,
            TestMinigame.ID,
            TestMinigame.codec()
        )
    }
}
