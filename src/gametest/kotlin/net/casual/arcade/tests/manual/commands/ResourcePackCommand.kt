package net.casual.arcade.tests.manual.commands

import com.google.common.collect.HashMultimap
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.*
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.pack.host.GlobalPackHost
import net.casual.arcade.pack.host.HostedPackRef
import net.casual.arcade.pack.generation.BuiltInResourcePacks
import net.casual.arcade.pack.generation.PackDefinition
import net.casual.arcade.pack.generation.utils.add
import net.casual.arcade.pack.utils.ResourcePackUtils.sendResourcePack
import net.casual.arcade.pack.utils.ResourcePackUtils.toPackInfo
import net.casual.arcade.tests.manual.resource_pack.TestResourcePacks
import net.casual.arcade.utils.coroutine.launch
import net.casual.arcade.utils.server.players
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

@Suppress("Unused")
object ResourcePackCommand: CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("resource-pack") {
            literal("host") {
                argument("name", StringArgumentType.greedyString()) {
                    suggests { TestResourcePacks.names() }
                    executes(::hostPack)
                }
            }
        }
    }

    private fun hostPack(context: CommandContext<CommandSourceStack>): Int {
        val name = StringArgumentType.getString(context, "name")
        val hosted = TestResourcePacks.pop(name).map(this::host)
        if (hosted.isEmpty()) {
            return context.source.fail("Failed to host pack $name, there were no resource packs under that name")
        }
        val server = context.source.server
        GlobalEventHandler.Server.register<PlayerJoinEvent> { (player) ->
            for (pack in hosted) {
                server.launch {
                    player.sendResourcePack(pack.await().toPackInfo())
                }
            }
        }

        server.launch {
            val packs = hosted.map { pack -> pack.await().toPackInfo() }
            for (player in server.players) {
                for (pack in packs) {
                    player.sendResourcePack(pack)
                }
            }
        }
        return context.source.success("Successfully hosting pack $name")
    }

    private fun host(pack: PackDefinition): HostedPackRef {
        return GlobalPackHost.add(pack)
    }
}