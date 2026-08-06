package net.casual.arcade.test.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.*
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.virtual.visuals.camera.CameraPath
import net.casual.arcade.virtual.visuals.utils.startObservingAndSendPackets
import net.casual.arcade.virtual.visuals.utils.stopObservingAndSendPackets
import net.casual.arcade.virtual.visuals.camera.CameraPathInterpolator
import net.casual.arcade.virtual.visuals.camera.VirtualCamera
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.TimeArgument

@Suppress("unused")
object PlayerCameraTestCommand: CommandTree<CommandSourceStack> {
    private lateinit var camera: VirtualCamera

    private var path = CameraPath.Builder()

    fun registerEvents() {
        GlobalEventHandler.Server.register<ServerTickEvent> { (server) ->
            if (this::camera.isInitialized) {
                this.camera.tick()
            }
        }
    }

    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("player-camera-test") {
            literal("start") {
                argument("loop", BoolArgumentType.bool()) {
                    executes(::start)
                }
            }
            literal("set-start") {
                executes(::setStart)
            }
            literal("add-pos") {
                argument("duration", TimeArgument.time()) {
                    executes { context -> addPos(context) }
                }
            }
            literal("set-interpolator") {
                literal("linear") {
                    executes { context -> setInterpolator(context, CameraPathInterpolator.Linear) }
                }
                literal("catmull") {
                    executes { context -> setInterpolator(context, CameraPathInterpolator.CatmullRom) }
                }
                literal("gaussian") {
                    executes { context -> setInterpolator(context, CameraPathInterpolator.Gaussian()) }
                    argument("variance", DoubleArgumentType.doubleArg(0.0, 1.0)) {
                        executes { context ->
                            val variance = DoubleArgumentType.getDouble(context, "variance")
                            setInterpolator(context, CameraPathInterpolator.Gaussian(variance))
                        }
                    }
                }
            }
            literal("reset") {
                executes(::reset)
            }
            literal("exit") {
                executes(::exit)
            }
        }
    }

    private fun start(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        val loop = BoolArgumentType.getBool(context, "loop")
        val camera = this.getOrCreateCamera(context.source)
        camera.startObservingAndSendPackets(player.asObserver())
        camera.setPath(this.path.build())
        camera.startPath(loop)
    }

    private fun setStart(context: CommandContext<CommandSourceStack>) {
        this.path.setStart(context.source.location)
    }

    private fun addPos(context: CommandContext<CommandSourceStack>): Int {
        val duration = IntegerArgumentType.getInteger(context, "duration").Ticks
        this.path.addPoint(context.source.location, duration)
        return Command.SINGLE_SUCCESS
    }

    @Suppress("unused")
    private fun setInterpolator(context: CommandContext<CommandSourceStack>, interpolator: CameraPathInterpolator): Int {
        this.path.setInterpolator(interpolator)
        return Command.SINGLE_SUCCESS
    }

    @Suppress("unused")
    private fun reset(context: CommandContext<CommandSourceStack>) {
        this.path = CameraPath.Builder()
    }

    private fun exit(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        val camera = this.getOrCreateCamera(context.source)
        camera.stopObservingAndSendPackets(player.asObserver())
    }

    private fun getOrCreateCamera(source: CommandSourceStack): VirtualCamera {
        if (!this::camera.isInitialized) {
            this.camera = VirtualCamera(source.locationWithLevel)
        }
        return this.camera
    }
}