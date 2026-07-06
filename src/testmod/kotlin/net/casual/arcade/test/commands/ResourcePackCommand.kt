package net.casual.arcade.test.commands

import com.google.common.collect.HashMultimap
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.future.await
import net.casual.arcade.commands.*
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.host.GlobalPackHost
import net.casual.arcade.host.PackHost
import net.casual.arcade.resources.ArcadeResourcePacks
import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.utils.ResourcePackUtils.addPack
import net.casual.arcade.resources.utils.ResourcePackUtils.sendResourcePack
import net.casual.arcade.resources.utils.ResourcePackUtils.toPackInfo
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.coroutine.launch
import net.casual.arcade.utils.server.players
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

@Suppress("Unused")
object ResourcePackCommand: CommandTree<CommandSourceStack> {
    private val registered = HashMultimap.create<String, NamedResourcePackCreator>()

    init {
        this.register("player_heads", ArcadeResourcePacks.PIXEL_FONT_PACK, ArcadeResourcePacks.SPACING_FONT_PACK)
        this.register("boundary", ArcadeResourcePacks.BOUNDARY_SHADER_PACK)
    }

    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("resource-pack") {
            literal("host") {
                argument("name", StringArgumentType.greedyString()) {
                    suggests { registered.keys() }
                    executes(::hostPack)
                }
            }
        }
    }

    private fun hostPack(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val hosted = this.registered.removeAll(name).map(this::host)
        if (hosted.isEmpty()) {
            return context.source.fail("Failed to host pack $name, there were no resource packs under that name")
        }
        GlobalEventHandler.Server.register<PlayerJoinEvent> { (player) ->
            for (pack in hosted) {
                player.sendResourcePack(pack.value.toPackInfo())
            }
        }

        val server = context.source.server
        server.launch {
            val packs = hosted.map { pack -> pack.future.await().toPackInfo() }
            for (player in server.players) {
                for (pack in packs) {
                    player.sendResourcePack(pack)
                }
            }
        }
        return context.source.success("Successfully hosting pack $name")
    }

    private fun register(name: String, vararg packs: NamedResourcePackCreator) {
        this.registered.putAll(name, packs.toList())
    }

    private fun host(pack: NamedResourcePackCreator): PackHost.HostedPackRef {
        return GlobalPackHost.addPack(ArcadeUtils.path.resolve("testing-packs"), pack)
    }
}