package net.casual.arcade.test

import net.casual.arcade.boundary.tracker.TrackedBorder
import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.host.GlobalPackHost
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.resources.ArcadeResourcePacks
import net.casual.arcade.resources.utils.ResourcePackUtils.addPack
import net.casual.arcade.resources.utils.ResourcePackUtils.sendResourcePack
import net.casual.arcade.resources.utils.ResourcePackUtils.toPackInfo
import net.casual.arcade.test.command.LevelBoundaryCommand
import net.casual.arcade.test.minigame.TestMinigame
import net.casual.arcade.utils.ArcadeUtils
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

        TrackedBorder(0.0, 0.0, 0.0)

        this.boundaryTests()
    }

    private fun boundaryTests() {
        val boundary by GlobalPackHost.addPack(
            ArcadeUtils.path.resolve("testing-packs"),
            ArcadeResourcePacks.BOUNDARY_SHADER
        )
        GlobalEventHandler.Server.register<PlayerJoinEvent> {
            it.player.sendResourcePack(boundary.toPackInfo())
        }
    }
}
