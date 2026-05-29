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
import net.casual.arcade.test.minigame.TestMinigame
import net.casual.arcade.test.resource_pack.ResourcePackTests
import net.casual.arcade.utils.ArcadeUtils
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry

object ArcadeTest: ModInitializer {
    override fun onInitialize() {
        ArcadeTestCommand.registerEvents()

        GlobalEventHandler.Server.register<ServerRegisterCommandEvent> {
            it.register(ArcadeTestCommand)
        }

        Registry.register(
            MinigameRegistries.MINIGAME_FACTORY,
            TestMinigame.ID,
            TestMinigame.codec()
        )

        ResourcePackTests.run()
    }
}
