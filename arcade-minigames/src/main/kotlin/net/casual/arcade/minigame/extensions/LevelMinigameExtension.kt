/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.extensions

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.casual.arcade.extensions.Extension
import net.casual.arcade.minigame.Minigame
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import java.util.*

internal class LevelMinigameExtension(
    val level: ServerLevel
): Extension {
    private var minigames = ReferenceLinkedOpenHashSet<Minigame>()

    internal fun getMinigames(): Set<Minigame> {
        return Collections.unmodifiableSet(this.minigames)
    }

    internal fun getMinigames(pos: BlockPos): Set<Minigame> {
        if (this.minigames.isEmpty()) {
            return emptySet()
        }
        // Implement a special case for when there is only one minigame, this is most common
        if (this.minigames.size == 1) {
            val minigame = this.minigames.first()
            if (minigame.levels.has(this.level, pos)) {
                return setOf(minigame)
            }
            return emptySet()
        }
        return this.minigames.filterTo(ReferenceLinkedOpenHashSet()) { it.levels.has(this.level, pos) }
    }

    internal fun addMinigame(minigame: Minigame) {
        this.minigames.add(minigame)
    }

    internal fun removeMinigame(minigame: Minigame) {
        this.minigames.remove(minigame)
    }
}