/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.extensions

import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer

internal class PlayerMinigameExtension(
    owner: ServerPlayer
): PlayerExtension(owner), DataExtension {
    private var minigame: Minigame? = null

    internal fun getMinigame(): Minigame? {
        return this.minigame
    }

    internal fun setMinigame(minigame: Minigame) {
        this.minigame?.players?.remove(this.player)
        this.minigame = minigame
    }

    internal fun removeMinigame() {
        this.minigame = null
    }

    override fun getName(): String {
        return "${ArcadeUtils.MOD_ID}_minigame_extension"
    }

    override fun serialize(): Tag {
        val tag = CompoundTag()
        val minigame = this.minigame
        if (minigame != null) {
            tag.putUUID("minigame", minigame.uuid)
        }
        return tag
    }

    override fun deserialize(element: Tag) {
        element as CompoundTag
        if (element.hasUUID("minigame")) {
            val minigame = Minigames.get(element.getUUID("minigame"))
            this.minigame = minigame
            if (minigame == null) {
                ArcadeUtils.logger.warn("Player ${this.player.scoreboardName} was part of an old minigame...")
                return
            }

            // We add the player in the JoinEvent.
            // See in MinigameUtils#registerEvents()
        }
    }
}