package net.casual.arcade.minigame.ready

import net.casual.arcade.commands.function
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Nameable
import net.minecraft.world.scores.PlayerTeam
import org.jetbrains.annotations.ApiStatus.OverrideOnly

public interface ReadyHandler<T> {
    @OverrideOnly
    public fun broadcastReadyCheck(receiver: T, ready: () -> Unit, notReady: () -> Unit)

    @OverrideOnly
    public fun onReady(readier: T, previous: ReadyState): Boolean

    @OverrideOnly
    public fun onNotReady(readier: T, previous: ReadyState): Boolean

    @OverrideOnly
    public fun onAllReady()

    @OverrideOnly
    public fun format(readier: T): Component
}

public abstract class MinigameReadyHandler<T>(
    protected val minigame: Minigame
): ReadyHandler<T> {
    protected open fun broadcast(message: Component) {
        this.minigame.chat.broadcast(message)
    }

    protected open fun broadcastTo(message: Component, player: ServerPlayer) {
        this.minigame.chat.broadcastTo(message, player)
    }

    protected open fun broadcastTo(message: Component, players: Iterable<ServerPlayer>) {
        for (player in players) {
            this.broadcastTo(message, player)
        }
    }
}

public open class MinigamePlayerReadyHandler(
    minigame: Minigame
): MinigameReadyHandler<ServerPlayer>(minigame) {
    override fun broadcastReadyCheck(receiver: ServerPlayer, ready: () -> Unit, notReady: () -> Unit) {
        val yes = Component.translatable("minigame.ready.yes").lime().function { ready.invoke() }
        val no = Component.translatable("minigame.ready.no").red().function { notReady.invoke() }
        this.broadcastTo(Component.translatable("minigame.ready.player.broadcast", yes, no), receiver)
    }

    override fun onReady(readier: ServerPlayer, previous: ReadyState): Boolean {
        val message = Component.translatable("minigame.ready.player.ready", this.format(readier)).lime()
        this.broadcast(message)
        return true
    }

    override fun onNotReady(readier: ServerPlayer, previous: ReadyState): Boolean {
        val message = Component.translatable("minigame.ready.player.notReady", this.format(readier)).red()
        this.broadcast(message)
        return true
    }

    override fun onAllReady() {
        this.broadcast(Component.translatable("minigame.ready.player.all").lime())
    }

    override fun format(readier: ServerPlayer): Component {
        return (readier as Nameable).displayName
    }
}

public open class MinigameTeamReadyHandler(
    minigame: Minigame
): MinigameReadyHandler<PlayerTeam>(minigame) {
    override fun broadcastReadyCheck(receiver: PlayerTeam, ready: () -> Unit, notReady: () -> Unit) {
        val yes = Component.translatable("minigame.ready.yes").lime().function { ready.invoke() }
        val no = Component.translatable("minigame.ready.no").red().function { notReady.invoke() }
        val message = Component.translatable("minigame.ready.team.broadcast", yes, no)
        this.broadcastTo(message, receiver.getOnlinePlayers())
    }

    override fun onReady(readier: PlayerTeam, previous: ReadyState): Boolean {
        val message = Component.translatable("minigame.ready.team.ready", this.format(readier)).lime()
        this.broadcast(message)
        return true
    }

    override fun onNotReady(readier: PlayerTeam, previous: ReadyState): Boolean {
        val message = Component.translatable("minigame.ready.team.notReady", this.format(readier)).red()
        this.broadcast(message)
        return true
    }

    override fun onAllReady() {
        this.broadcast(Component.translatable("minigame.ready.team.all").lime())
    }

    override fun format(readier: PlayerTeam): Component {
        return readier.formattedDisplayName
    }
}