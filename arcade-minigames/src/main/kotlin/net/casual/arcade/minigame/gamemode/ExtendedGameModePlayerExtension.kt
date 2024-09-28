package net.casual.arcade.minigame.gamemode

import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType

internal class ExtendedGameModePlayerExtension(
    player: ServerPlayer
): PlayerExtension(player), DataExtension {
    private var gameMode = ExtendedGameMode.None
    private var changedGameMode = false

    fun getGameMode(): ExtendedGameMode {
        return this.gameMode
    }

    fun setGameMode(mode: ExtendedGameMode) {
        if (mode != this.gameMode) {
            this.forceSetGameMode(mode)
        }
    }

    fun forceSetGameMode(mode: ExtendedGameMode) {
        this.changedGameMode = true

        mode.set(this.player)
        this.gameMode = mode
    }

    fun setGameModeFromVanilla(type: GameType) {
        this.gameMode = ExtendedGameMode.fromVanilla(type)
    }

    override fun getName(): String {
        return "${ArcadeUtils.MOD_ID}_extended_game_mode"
    }

    override fun serialize(): Tag? {
        return StringTag.valueOf(this.gameMode.name)
    }

    override fun deserialize(element: Tag) {
        val gameMode = enumValueOf<ExtendedGameMode>(element.asString)
        GlobalTickedScheduler.later {
            if (!this.changedGameMode) {
                this.setGameMode(gameMode)
            }
        }
    }
}