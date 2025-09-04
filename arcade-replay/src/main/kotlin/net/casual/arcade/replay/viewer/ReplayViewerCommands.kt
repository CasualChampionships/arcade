/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.viewer

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.RootCommandNode
import net.casual.arcade.commands.*
import net.casual.arcade.replay.viewer.ReplayViewerUtils.getViewingReplay
import net.casual.arcade.utils.DateTimeUtils.formatHHMMSS
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSource
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.TimeArgument
import net.minecraft.commands.synchronization.SuggestionProviders
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundCommandsPacket
import net.minecraft.network.protocol.game.ClientboundCommandsPacket.NodeInspector
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.resources.ResourceLocation
import java.util.function.Consumer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public object ReplayViewerCommands {
    private val dispatcher = CommandDispatcher<CommandSourceStack>()

    init {
        this.dispatcher.register(this.createReplayViewCommand())
    }

    public fun sendCommandPacket(consumer: Consumer<Packet<ClientGamePacketListener>>) {
        // TODO: Add some way to add configurable permissions to commands
        consumer.accept(ClientboundCommandsPacket(this.dispatcher.root, ReplayViewerNodeInspector))
    }

    public fun handleCommand(command: String, viewer: ReplayViewer) {
        viewer.server.execute {
            val source = viewer.player.createCommandSourceStack().withSource(ReplayViewerCommandSource(viewer))
            val result = this.dispatcher.parse(command, source)
            viewer.server.commands.performCommand(result, command)
        }
    }

    private fun createReplayViewCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("replay") {
            literal("view") {
                literal("close") {
                    executes(::stopViewingReplay)
                }
                literal("speed") {
                    argument("multiplier", FloatArgumentType.floatArg(0.05F)) {
                        executes(::setViewingReplaySpeed)
                    }
                }
                literal("pause") {
                    executes { pauseViewingReplay(it, true) }
                }
                literal("unpause") {
                    executes { pauseViewingReplay(it, false) }
                }
                literal("restart") {
                    executes(::restartViewingReplay)
                }
                literal("progress") {
                    literal("show") {
                        executes(::showReplayProgress)
                    }
                    literal("hide") {
                        executes(::hideReplayProgress)
                    }
                }
                literal("camera") {
                    literal("reset") {
                        executes(::resetCamera)
                    }
                }
                literal("jump") {
                    literal("to") {
                        literal("marker") {
                            literal("unnamed") {
                                argument("offset", TimeArgument.time(Int.MIN_VALUE)) {
                                    executes { jumpToMarker(it, name = null) }
                                }
                                executes { jumpToMarker(it, null, 0) }
                            }
                            literal("named") {
                                argument("name", StringArgumentType.string()) {
                                    argument("offset", TimeArgument.time(Int.MIN_VALUE)) {
                                        executes(::jumpToMarker)
                                    }
                                    executes { jumpToMarker(it, offset = 0) }
                                }
                            }
                        }
                        literal("timestamp") {
                            argument("timestamp", TimeArgument.time()) {
                                executes(::jumpTo)
                            }
                        }
                    }
                }
                literal("list") {
                    literal("markers") {
                        executes(::listMarkers)
                    }
                }
            }
        }
    }

    private fun stopViewingReplay(context: CommandContext<CommandSourceStack>): Int {
        context.source.getReplayViewer().stop()
        return Command.SINGLE_SUCCESS
    }

    private fun setViewingReplaySpeed(context: CommandContext<CommandSourceStack>): Int {
        val speed = FloatArgumentType.getFloat(context, "multiplier")
        val viewer = context.source.getReplayViewer()
        viewer.setSpeed(speed)
        return context.source.success("Successfully set replay speed to $speed")
    }

    private fun pauseViewingReplay(context: CommandContext<CommandSourceStack>, paused: Boolean): Int {
        val viewer = context.source.getReplayViewer()
        if (viewer.setPaused(paused)) {
            return context.source.success("Successfully paused replay")
        }
        return context.source.fail("Replay was already paused")
    }

    private fun restartViewingReplay(context: CommandContext<CommandSourceStack>): Int {
        val viewer = context.source.getReplayViewer()
        viewer.jumpTo(Duration.ZERO)
        return context.source.success("Successfully restarted replay")
    }

    private fun showReplayProgress(context: CommandContext<CommandSourceStack>): Int {
        val viewer = context.source.getReplayViewer()
        if (viewer.showProgress()) {
            return context.source.success("Successfully showing replay progress bar")
        }
        return context.source.fail("Progress bar was already shown")
    }

    private fun hideReplayProgress(context: CommandContext<CommandSourceStack>): Int {
        val viewer = context.source.getReplayViewer()
        if (viewer.hideProgress()) {
            return context.source.success("Successfully hidden replay progress bar")
        }
        return context.source.fail("Progress bar was already hidden")
    }

    private fun resetCamera(context: CommandContext<CommandSourceStack>): Int {
        val viewer = context.source.getReplayViewer()
        viewer.resetCamera()
        return context.source.success("Successfully reset camera")
    }

    private fun jumpToMarker(
        context: CommandContext<CommandSourceStack>,
        name: String? = StringArgumentType.getString(context, "name"),
        offset: Int = IntegerArgumentType.getInteger(context, "offset")
    ): Int {
        val viewer = context.source.getReplayViewer()
        if (viewer.jumpToMarker(name, (offset * 50).milliseconds)) {
            return context.source.success("Successfully jumped to marker")
        }
        return context.source.fail("No such marker found, or offset too large")
    }

    private fun jumpTo(context: CommandContext<CommandSourceStack>): Int {
        val viewer = context.source.getReplayViewer()
        val time = IntegerArgumentType.getInteger(context, "timestamp")
        if (viewer.jumpTo((time * 50).milliseconds)) {
            return context.source.success("Successfully jumped to timestamp")
        }
        return context.source.fail("Timestamp provided was outside of recording")
    }

    private fun listMarkers(context: CommandContext<CommandSourceStack>): Int {
        val viewer = context.source.getReplayViewer()
        val markers = viewer.getMarkers()
        if (markers.isEmpty()) {
            return context.source.success("Replay has no markers")
        }
        val component = Component.empty()
        val iter = markers.iterator()
        for (marker in iter) {
            val time = marker.timestamp.formatHHMMSS()
            component.append(Component.literal(time).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
            component.append(": ")
            component.append(Component.literal(marker.name ?: "Unnamed").withStyle(ChatFormatting.GREEN))
            if (iter.hasNext()) {
                component.append("\n")
            }
        }
        return context.source.success(component)
    }

    private fun CommandSourceStack.getReplayViewer(): ReplayViewer {
        val player = this.playerOrException
        return player.connection.getViewingReplay()
            ?: throw IllegalStateException("Player not viewing replay managed to execute this command!?")
    }

    private class ReplayViewerCommandSource(private val viewer: ReplayViewer): CommandSource {
        override fun sendSystemMessage(component: Component) {
            this.viewer.send(ClientboundSystemChatPacket(component, false))
        }

        override fun acceptsSuccess(): Boolean {
            return true
        }

        override fun acceptsFailure(): Boolean {
            return true
        }

        override fun shouldInformAdmins(): Boolean {
            return true
        }
    }

    private object ReplayViewerNodeInspector: NodeInspector<CommandSourceStack> {
        override fun suggestionId(node: ArgumentCommandNode<CommandSourceStack, *>): ResourceLocation? {
            val suggestions = node.customSuggestions
            return if (suggestions != null) SuggestionProviders.getName(suggestions) else null
        }

        override fun isExecutable(node: CommandNode<CommandSourceStack>): Boolean {
            return node.command != null
        }

        override fun isRestricted(node: CommandNode<CommandSourceStack>): Boolean {
            return false
        }
    }
}