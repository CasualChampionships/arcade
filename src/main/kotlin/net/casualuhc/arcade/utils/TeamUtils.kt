package net.casualuhc.arcade.utils

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer
import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.extensions.Extension
import net.casualuhc.arcade.extensions.ExtensionHolder
import net.casualuhc.arcade.utils.ExtensionUtils.addExtension
import net.casualuhc.arcade.utils.ExtensionUtils.getExtension
import net.casualuhc.arcade.utils.ExtensionUtils.getExtensions
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

@Suppress("unused")
object TeamUtils {
    @JvmStatic
    fun teams(): Collection<PlayerTeam> {
        return Arcade.server.scoreboard.playerTeams
    }

    @JvmStatic
    fun forEachTeam(consumer: Consumer<PlayerTeam>) {
        for (team in this.teams()) {
            consumer.accept(team)
        }
    }

    @JvmStatic
    fun Team.asPlayerTeam(): PlayerTeam {
        return this as PlayerTeam
    }

    @JvmStatic
    fun Team.addExtension(extension: Extension) {
        (this as ExtensionHolder).addExtension(extension)
    }

    @JvmStatic
    fun <T: Extension> Team.getExtension(type: Class<T>): T {
        return (this as ExtensionHolder).getExtension(type)
    }

    @JvmStatic
    fun Team.getExtensions(): Collection<Extension> {
        return (this as ExtensionHolder).getExtensions()
    }
}