package net.casual.arcade.test.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.plus
import net.casual.arcade.visuals.tab.PlayerListDisplay
import net.casual.arcade.visuals.tab.VanillaPlayerListEntries
import net.casual.arcade.visuals.utils.elements.ComponentElements
import net.casual.arcade.visuals.utils.elements.component.MSPTComponentElement
import net.casual.arcade.visuals.utils.elements.component.TPSComponentElement
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

@Suppress("unused")
object TabCommand: CommandTree<CommandSourceStack> {
    private val display = PlayerListDisplay(VanillaPlayerListEntries())

    fun registerEvents() {
        GlobalEventHandler.Server.register<ServerTickEvent> { (server) ->
            this.display.tick(server)
        }
        GlobalEventHandler.Server.register<PlayerJoinEvent> { (player) ->
            this.display.addPlayer(player)
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
        this.display.setDisplay(
            ComponentElements.of { literal("Displaying TPS") + nl },
            TPSComponentElement.merge(MSPTComponentElement) { tps, mspt ->
                Component { empty() + nl + tps + nl + mspt + nl }
            }
        )
    }
}