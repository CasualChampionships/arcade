package net.casual.arcade.test

import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.host.GlobalPackHost
import net.casual.arcade.host.PackHost
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.resources.ArcadeResourcePacks
import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.utils.ResourcePackUtils.addPack
import net.casual.arcade.resources.utils.ResourcePackUtils.sendResourcePack
import net.casual.arcade.resources.utils.ResourcePackUtils.toPackInfo
import net.casual.arcade.test.command.LevelBoundaryCommand
import net.casual.arcade.test.command.PlayerHeadCommand
import net.casual.arcade.test.minigame.TestMinigame
import net.casual.arcade.utils.ArcadeUtils
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry

object ArcadeTest: ModInitializer {
    override fun onInitialize() {
        GlobalEventHandler.Server.register<ServerRegisterCommandEvent> {
            it.register(LevelBoundaryCommand, PlayerHeadCommand)
        }

        Registry.register(
            MinigameRegistries.MINIGAME_FACTORY,
            TestMinigame.ID,
            TestMinigame.codec()
        )

        this.playerHeadTests()
        this.boundaryTests()
    }

    private fun playerHeadTests() {
        val pixel by this.host(ArcadeResourcePacks.PIXEL_FONT_PACK)
        val space by this.host(ArcadeResourcePacks.SPACING_FONT_PACK)
        GlobalEventHandler.Server.register<PlayerJoinEvent> { (player) ->
            player.sendResourcePack(pixel.toPackInfo())
            player.sendResourcePack(space.toPackInfo())
        }
    }

    private fun boundaryTests() {
        val boundary by this.host(ArcadeResourcePacks.BOUNDARY_SHADER_PACK)
        GlobalEventHandler.Server.register<PlayerJoinEvent> { (player) ->
            player.sendResourcePack(boundary.toPackInfo())
        }
    }

    private fun host(pack: NamedResourcePackCreator): PackHost.HostedPackRef {
        return GlobalPackHost.addPack(ArcadeUtils.path.resolve("testing-packs"), pack)
    }
}
