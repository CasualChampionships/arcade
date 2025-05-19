package net.casual.arcade.test

import com.mojang.brigadier.Command
import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment
import eu.pb4.polymer.virtualentity.api.elements.EntityElement
import eu.pb4.polymer.virtualentity.api.elements.SimpleEntityElement
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.registerLiteral
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.player.PlayerChatEvent
import net.casual.arcade.events.server.player.PlayerTickEvent
import net.casual.arcade.host.GlobalPackHost
import net.casual.arcade.host.PackHost
import net.casual.arcade.minigame.extensions.PlayerMovementRestrictionExtension.Companion.restrictMovement
import net.casual.arcade.minigame.extensions.PlayerMovementRestrictionExtension.Companion.unrestrictMovement
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.minigame.utils.MinigameRegistryKeys
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.resources.ArcadeResourcePacks
import net.casual.arcade.resources.utils.ResourcePackUtils.addPack
import net.casual.arcade.resources.utils.ResourcePackUtils.sendResourcePack
import net.casual.arcade.resources.utils.ResourcePackUtils.toPackInfo
import net.casual.arcade.utils.ComponentUtils.wrap
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.locationWithLevel
import net.casual.arcade.utils.teleportTo
import net.casual.arcade.visuals.screen.PlayerInventoryViewGui
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.phys.Vec3
import javax.net.ssl.SSLContext

object ArcadeTest: ModInitializer {
    override fun onInitialize() {
        ArcadeResourcePacks.SPACING_FONT_PACK.buildTo(FabricLoader.getInstance().configDir)

        GlobalEventHandler.Server.register<ServerRegisterCommandEvent> {
            it.dispatcher.registerLiteral("view-inventory") {
                argument("target", EntityArgument.player()) {
                    executes { ctx ->
                        val target = EntityArgument.getPlayer(ctx, "target")
                        PlayerInventoryViewGui(target, ctx.source.playerOrException).open()
                        Command.SINGLE_SUCCESS
                    }
                }
            }
            it.dispatcher.registerLiteral("restrict-movement") {
                executes { ctx ->
                    ctx.source.playerOrException.restrictMovement(true)
                    1
                }
            }
            it.dispatcher.registerLiteral("unrestrict-movement") {
                executes { ctx ->
                    ctx.source.playerOrException.unrestrictMovement()
                    1
                }
            }
        }

        Registry.register(
            MinigameRegistries.MINIGAME_FACTORY,
            TestMinigame.ID,
            TestMinigame.codec()
        )

        val spacing by GlobalPackHost.addPack(
            FabricLoader.getInstance().configDir.resolve("arcade-testing-packs"),
            ArcadeResourcePacks.SPACING_FONT_PACK
        )

        GlobalEventHandler.Server.register<PlayerChatEvent> {
            it.format { chat ->
                chat.copy(prefix = chat.prefix.wrap().append(Component.literal("[x]")))
            }
        }
    }
}

object Example: ModInitializer {
    override fun onInitialize() = GlobalEventHandler.Server.register<ServerLoadedEvent> { (server) ->

    }
}