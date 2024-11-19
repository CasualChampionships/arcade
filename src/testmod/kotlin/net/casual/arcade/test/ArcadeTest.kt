package net.casual.arcade.test

import com.mojang.brigadier.Command
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.registerLiteral
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.resources.ArcadeResourcePacks
import net.casual.arcade.visuals.screen.PlayerInventoryViewGui
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.core.Registry

object ArcadeTest: ModInitializer {
    override fun onInitialize() {
        ArcadeResourcePacks.SPACING_FONT_PACK.buildTo(FabricLoader.getInstance().configDir)

        GlobalEventHandler.register<ServerRegisterCommandEvent> {
            it.dispatcher.registerLiteral("view-inventory") {
                argument("target", EntityArgument.player()) {
                    executes { ctx ->
                        val target = EntityArgument.getPlayer(ctx, "target")
                        PlayerInventoryViewGui(target, ctx.source.playerOrException).open()
                        Command.SINGLE_SUCCESS
                    }
                }
            }
        }

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