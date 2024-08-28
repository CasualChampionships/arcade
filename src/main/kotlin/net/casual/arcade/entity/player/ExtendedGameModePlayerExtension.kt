package net.casual.arcade.entity.player

import net.casual.arcade.Arcade
import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType

internal class ExtendedGameModePlayerExtension(
    player: ServerPlayer
): PlayerExtension(player), DataExtension {
    private var gameMode = ExtendedGameMode.Survival

    fun getGameMode(): ExtendedGameMode {
        return this.gameMode
    }

    fun setGameMode(mode: ExtendedGameMode) {
        if (mode != this.gameMode) {
            mode.set(this.player)
            this.gameMode = mode
        }
    }

    fun setGameModeFromVanilla(type: GameType) {
        this.gameMode = ExtendedGameMode.fromVanilla(type)
    }

    override fun getName(): String {
        return "${Arcade.MOD_ID}_extended_game_mode"
    }

    override fun serialize(): Tag? {
        return StringTag.valueOf(this.gameMode.name)
    }

    override fun deserialize(element: Tag) {
        val gameMode = enumValueOf<ExtendedGameMode>(element.asString)
        GlobalTickedScheduler.later {
            this.setGameMode(gameMode)
        }
    }
}