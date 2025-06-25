/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.gamemode

import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

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
            this.forceSetGameMode(mode, this.player)
        }
    }

    // We take in a player parameter here because the
    // `connection.player` reference may be outdated...
    fun forceSetGameMode(mode: ExtendedGameMode, player: ServerPlayer) {
        if (mode == ExtendedGameMode.None) {
            return
        }

        this.changedGameMode = true

        mode.set(player)
        this.gameMode = mode
    }

    fun setGameModeFromVanilla(type: GameType) {
        this.changedGameMode = true

        this.gameMode = ExtendedGameMode.fromVanilla(type)
    }

    override fun getId(): ResourceLocation {
        return ArcadeUtils.id("extended_game_mode")
    }

    override fun serialize(output: ValueOutput) {
        output.store("game_mode", ExtendedGameMode.CODEC, this.gameMode)
    }

    override fun deserialize(input: ValueInput) {
        val gameMode = input.read("game_mode", ExtendedGameMode.CODEC)
            .orElse(ExtendedGameMode.None)

        this.changedGameMode = false
        GlobalTickedScheduler.later {
            if (!this.changedGameMode) {
                this.setGameMode(gameMode)
            }
        }
    }
}