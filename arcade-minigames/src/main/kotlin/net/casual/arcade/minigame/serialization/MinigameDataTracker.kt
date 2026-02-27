/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.serialization

import net.casual.arcade.minigame.Minigame
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.server.level.ServerPlayer
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Deprecated("For removal")
public class MinigameDataTracker(
    private val minigame: Minigame
) {
    @Deprecated("For removal")
    public var startTime: Instant = Instant.DISTANT_PAST
        private set
    @Deprecated("For removal")
    public var endTime: Instant = Instant.DISTANT_FUTURE
        private set

    @Deprecated("For removal")
    public fun start() {
        if (this.startTime == Instant.DISTANT_PAST) {
            this.startTime = Clock.System.now()
        }
    }

    @Deprecated("For removal")
    public fun end() {
        if (this.endTime != Instant.DISTANT_FUTURE) {
            return
        }

        for (player in this.minigame.players) {
            this.updatePlayer(player)
        }

        this.endTime = Clock.System.now()
    }

    @Deprecated("For removal")
    public fun updatePlayer(player: ServerPlayer) {

    }

    @Deprecated("For removal")
    public fun getAdvancements(uuid: UUID): List<AdvancementHolder> {
        return this.minigame.advancements.getFor(uuid).mapNotNull {
            this.minigame.advancements.get(it)
        }
    }
}