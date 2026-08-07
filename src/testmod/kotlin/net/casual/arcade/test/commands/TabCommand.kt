package net.casual.arcade.test.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerStartEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.plus
import net.casual.arcade.virtual.visuals.tab.DynamicVirtualPlayerList
import net.casual.arcade.virtual.visuals.tab.VanillaPlayerListEntries
import net.casual.arcade.virtual.visuals.utils.elements.ComponentElements
import net.casual.arcade.virtual.visuals.utils.elements.component.MSPTComponentElement
import net.casual.arcade.virtual.visuals.utils.elements.component.TPSComponentElement
import net.casual.arcade.virtual.visuals.utils.startObservingAndSendPackets
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

@Suppress("unused")
object TabCommand: CommandTree<CommandSourceStack> {
    private lateinit var display: DynamicVirtualPlayerList

    fun registerEvents() {
        GlobalEventHandler.Server.register<ServerStartEvent> { (server) ->
            this.display = DynamicVirtualPlayerList(server, VanillaPlayerListEntries())
        }
        GlobalEventHandler.Server.register<ServerTickEvent> {
            this.display.tick()
        }
        GlobalEventHandler.Server.register<PlayerJoinEvent> { (player) ->
            this.display.startObservingAndSendPackets(player.asObserver())
        }
    }

    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("tab") {
            literal("tps") {
                executes(::setDisplayToShowTps)
            }
        }
    }

    private fun setDisplayToShowTps(context: CommandContext<CommandSourceStack>) {
        this.display.setHeader(ComponentElements.of { literal("Displaying TPS") + nl })
        this.display.setFooter(TPSComponentElement.merge(MSPTComponentElement) { tps, mspt ->
            Component { empty() + nl + tps + nl + mspt + nl }
        })
    }
}
