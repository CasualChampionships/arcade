package net.casual.arcade.minigame.lobby

import com.google.gson.JsonObject
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.commands.*
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.area.PlaceableArea
import net.casual.arcade.minigame.commands.arguments.MinigameArgument
import net.casual.arcade.minigame.events.*
import net.casual.arcade.minigame.managers.MinigameLevelManager
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.arcade.minigame.utils.MinigameUtils.transferAdminAndSpectatorTeamsTo
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.task.impl.CancellableTask
import net.casual.arcade.utils.*
import net.casual.arcade.utils.ComponentUtils.command
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.join
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.JsonUtils.uuidOrNull
import net.casual.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casual.arcade.utils.PlayerUtils.ops
import net.casual.arcade.utils.PlayerUtils.resetExperience
import net.casual.arcade.utils.PlayerUtils.resetHealth
import net.casual.arcade.utils.PlayerUtils.resetHunger
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.utils.time.MinecraftTimeUnit
import net.casual.arcade.visuals.bossbar.TimerBossbar
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameRules
import net.minecraft.world.scores.PlayerTeam
import java.util.*

public open class LobbyMinigame(
    server: MinecraftServer,
    uuid: UUID,
    protected val area: PlaceableArea,
    protected val spawn: Location
): Minigame(server, uuid) {
    private var transferring: Boolean = false

    protected var bossbar: TimerBossbar = TimerBossbar.DEFAULT
        private set

    override val id: ResourceLocation = ID

    public var next: Minigame? = null
        set(value) {
            value?.tryInitialize()
            field = value
        }

    init {
        this.addLobbyProperties()

        this.bossbar.then(this::completeBossBar)
    }

    public open fun getTeamsToReady(): Collection<PlayerTeam> {
        return this.teams.getPlayingTeams()
    }

    public open fun getPlayersToReady(): Collection<ServerPlayer> {
        return this.players.playing
    }

    public open fun teleportToSpawn(player: ServerPlayer) {
        player.teleportTo(this.spawn)
    }

    public open fun teleportToLobby(player: ServerPlayer): Boolean {
        if (player.level() != this.spawn.level || !this.area.getEntityBoundingBox().contains(player.position())) {
            this.teleportToSpawn(player)
            return true
        }
        return false
    }

    public fun moveToNextMinigame() {
        if (this.transferring) {
            return
        }

        val next = this.next!!
        if (next.closed) {
            ArcadeUtils.logger.warn("Failed to move to next minigame ${next.id}, it was closed before starting!")
            this.next = null
            return
        }

        this.transferring = true
        val event = LobbyMoveToNextMinigameEvent(this, next)
        GlobalEventHandler.broadcast(event)

        val task = CancellableTask.of {
            this.transferAdminAndSpectatorTeamsTo(next)
            this.players.transferTo(next, players)
            next.start()

            this.setPhase(LobbyPhase.Waiting)
            this.next = null
            this.transferring = false
        }.ifCancelled {
            this.transferring = false
        }

        if (!event.delay.isZero) {
            this.scheduler.schedulePhased(event.delay, task)
        } else {
            task.run()
        }
    }

    override fun phases(): Collection<Phase<out Minigame>> {
        return LobbyPhase.entries
    }

    override fun load(data: JsonObject) {
        val uuid = data.uuidOrNull("next_minigame")
        if (uuid != null) {
            // Our minigame may not be deserialized yet
            GlobalTickedScheduler.later {
                this.next = Minigames.get(uuid)
            }
        }
    }

    override fun save(data: JsonObject) {
        val next = this.next
        if (next != null) {
            data.addProperty("next_minigame", next.uuid.toString())
        }
    }

    protected open fun startNextMinigame() {
        this.setPhase(LobbyPhase.Countdown)
    }

    protected fun setBossbar(bossbar: TimerBossbar) {
        this.bossbar = bossbar
        bossbar.then(this::completeBossBar)
    }

    internal fun getBossbar(): TimerBossbar {
        return this.bossbar
    }

    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        this.levels.add(this.area.level)
        this.levels.spawn = MinigameLevelManager.SpawnLocation.global(
            this.area.level,
            BlockPos.containing(this.spawn.position)
        )

        this.levels.setGameRules {
            resetToDefault()
            set(GameRules.RULE_DOINSOMNIA, false)
            set(GameRules.RULE_DOFIRETICK, false)
            set(GameRules.RULE_DOMOBSPAWNING, false)
            set(GameRules.RULE_FALL_DAMAGE, false)
            set(GameRules.RULE_DROWNING_DAMAGE, false)
            set(GameRules.RULE_DOENTITYDROPS, false)
            set(GameRules.RULE_WEATHER_CYCLE, false)
            set(GameRules.RULE_DO_TRADER_SPAWNING, false)
            set(GameRules.RULE_DOMOBLOOT, false)
            set(GameRules.RULE_DOBLOCKDROPS, false)
            set(GameRules.RULE_COMMANDBLOCKOUTPUT, false)
            set(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT, 0)
            set(GameRules.RULE_RANDOMTICKING, 0)
        }
        this.events.register<MinigameAddNewPlayerEvent> { (_, player) ->
            player.resetHealth()
            player.resetExperience()
            player.resetHunger()
            player.clearPlayerInventory()
        }
        this.events.register<MinigameAddPlayerEvent> { (_, player) ->
            this.teleportToLobby(player)
        }
        this.events.register<ServerTickEvent> {
            for (player in this.players.nonAdmins) {
                this.teleportToLobby(player)
            }
        }
        this.events.register<MinigameCloseEvent> {
            this.next?.close()
        }

        this.commands.register(this.createLobbyCommand())

        this.settings.pauseOnServerStop = false
        this.settings.canPvp.set(false)
        this.settings.canGetHungry.set(false)
        this.settings.canBreakBlocks.set(false)
        this.settings.canPlaceBlocks.set(false)
        this.settings.canDropItems.set(false)
        this.settings.canPickupItems.set(false)
        this.settings.canTakeDamage.set(false)
        this.settings.canAttackEntities.set(false)
        this.settings.canInteractAll = false
        this.settings.daylightCycle = 0

        this.area.replace()
    }

    private fun onReady() {
        val component = Component.translatable("minigame.lobby.ready.clickToStart").green().function {
            this.startNextMinigame()
        }
        val admins = ObjectOpenHashSet(this.players.admins)
        admins.addAll(this.players.ops())
        this.chat.broadcastTo(component, admins)
    }

    protected open fun createLobbyCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("lobby") {
            requiresAdminOrPermission()
            literal("next") {
                literal("settings") {
                    executes(::nextMinigameSettings)
                }
                literal("set") {
                    literal("existing") {
                        argument("minigame", MinigameArgument.minigame()) {
                            executes(::setNextMinigame)
                        }
                    }
                    literal("none") {
                        executes(::unsetNextMinigame)
                    }
                }
            }
            literal("place") {
                executes(::placeLobby)
            }
            literal("replace") {
                executes(::replaceLobby)
            }
            literal("delete") {
                executes(::deleteLobby)
            }
            literal("tp") {
                executes(::teleportToLobby)
            }
            literal("countdown") {
                executes(::startCountdown)
            }
            literal("ready") {
                literal("players") {
                    executes(::readyPlayers)
                }
                literal("teams") {
                    executes(::readyTeams)
                }
                literal("awaiting") {
                    executes(::awaitingReady)
                }
            }
            literal("start") {
                literal("in") {
                    argument("time", IntegerArgumentType.integer(1)) {
                        argument("unit", EnumArgument.enumeration<MinecraftTimeUnit>()) {
                            executes(::setTime)
                        }
                    }
                }
            }
        }
    }

    private fun nextMinigameSettings(context: CommandContext<CommandSourceStack>): Int {
        val next = this.next ?: throw NO_MINIGAME.create()
        val player = context.source.playerOrException
        next.settings.gui(player).open()
        return Command.SINGLE_SUCCESS
    }

    private fun setNextMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = MinigameArgument.getMinigame(context, "minigame")
        this.next = minigame
        return context.source.success("Successfully set the next minigame to ${minigame.id}")
    }

    private fun unsetNextMinigame(context: CommandContext<CommandSourceStack>): Int {
        this.next?.close()
        this.next = null
        return context.source.success("Successfully unset the next minigame")
    }

    private fun placeLobby(context: CommandContext<CommandSourceStack>): Int {
        this.area.place()
        return context.source.success("Successfully placed the lobby")
    }

    private fun replaceLobby(context: CommandContext<CommandSourceStack>): Int {
        this.area.replace()
        return context.source.success("Successfully replaced the lobby")
    }

    private fun deleteLobby(context: CommandContext<CommandSourceStack>): Int {
        this.area.removeAllButPlayers()
        return context.source.success("Successfully removed the lobby")
    }

    private fun teleportToLobby(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        this.teleportToLobby(player)
        return context.source.success("Successfully teleported to the lobby")
    }

    private fun startCountdown(context: CommandContext<CommandSourceStack>): Int {
        this.next ?: return context.source.fail("Cannot move to next minigame, it has not been set!")
        this.setPhase(LobbyPhase.Countdown)
        return context.source.success("Successfully started the countdown")
    }

    private fun readyPlayers(context: CommandContext<CommandSourceStack>): Int {
        this.next ?: return context.source.fail("Cannot ready for next minigame, it has not been set!")
        this.setPhase(LobbyPhase.Readying)
        this.ui.readier.arePlayersReady(this.getPlayersToReady()).then(this::onReady)
        return context.source.success("Successfully broadcasted ready check")
    }

    private fun readyTeams(context: CommandContext<CommandSourceStack>): Int {
        this.next ?: return context.source.fail("Cannot ready for next minigame, it has not been set!")
        this.setPhase(LobbyPhase.Readying)
        this.ui.readier.areTeamsReady(this.getTeamsToReady()).then(this::onReady)
        return context.source.success("Successfully broadcasted ready check")
    }

    private fun awaitingReady(context: CommandContext<CommandSourceStack>): Int {
        if (!this.ui.readier.isRunning()) {
            return context.source.fail("Not currently awaiting any players or teams to be ready")
        }
        val awaiting = this.ui.readier.getUnreadyFormatted(context.source.server)
        return context.source.success("Currently awaiting: ".literal().append(awaiting.join()))
    }

    private fun setTime(context: CommandContext<CommandSourceStack>): Int {
        val time = IntegerArgumentType.getInteger(context, "time")
        val unit = EnumArgument.getEnumeration(context, "unit", MinecraftTimeUnit::class.java)
        val duration = unit.duration(time)
        this.bossbar.setDuration(duration)
        return context.source.success("Countdown will begin in $time ${unit.name}")
    }

    private fun completeBossBar() {
        val message = Component.translatable("minigame.lobby.ready.finishedWaiting").append(" ")
        val teams = Component.translatable("minigame.lobby.ready.clickToReadyTeams").lime().command("/lobby ready teams")
        val players = Component.translatable("[Click to ready players]").lime().command("/lobby ready players")
        val component = message.append(teams).append(" ").append(players)

        this.chat.broadcastTo(component, this.players.admins)
    }

    private fun addLobbyProperties() {
        this.property("next_minigame") { this.next?.id?.toString() }
    }

    public companion object {
        private val NO_MINIGAME = SimpleCommandExceptionType(Component.translatable("minigame.lobby.command.noNextMinigame"))

        public val ID: ResourceLocation = ResourceUtils.arcade("lobby")
    }
}