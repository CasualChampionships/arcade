/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.component.impl

import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.server.player.PlayerDamageEvent
import net.casual.arcade.events.server.player.PlayerDeathEvent
import net.casual.arcade.events.server.player.PlayerHealEvent
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.events.server.player.PlayerTickEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.component.MinigameComponent
import net.casual.arcade.minigame.component.MinigameComponentFactory
import net.casual.arcade.minigame.component.MinigameComponentType
import net.casual.arcade.minigame.component.SerializableMinigameComponent
import net.casual.arcade.minigame.scope.MinigameScope
import net.casual.arcade.minigame.stats.ArcadeStats
import net.casual.arcade.minigame.stats.Stat.Companion.increment
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.player.getKillCreditWith
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

public class DefaultStatsComponent: SerializableMinigameComponent {
    override fun initialize(scope: MinigameScope) {
        val minigame = scope.minigame

        scope.register<PlayerTickEvent> { (player) ->
            minigame.stats.getOrCreateStat(player, ArcadeStats.PLAY_TIME).increment()
        }
        scope.register<PlayerJoinEvent> { (player) ->
            minigame.stats.getOrCreateStat(player, ArcadeStats.RELOGS).increment()
        }
        scope.register<PlayerDeathEvent> { event ->
            minigame.stats.getOrCreateStat(event.player, ArcadeStats.DEATHS).increment()

            val killer = event.player.getKillCreditWith(event.source)
            if (killer is ServerPlayer && minigame.players.has(killer)) {
                minigame.stats.getOrCreateStat(killer, ArcadeStats.KILLS).increment()
            }
        }
        scope.register<PlayerDamageEvent>(phase = BuiltInEventPhases.POST) { event ->
            val (player, source, amount) = event
            if (amount > 0 && amount < 3.4028235E37F) {
                minigame.stats.getOrCreateStat(player, ArcadeStats.DAMAGE_TAKEN).increment(amount)

                val attacker = source.entity
                if (attacker is ServerPlayer && minigame.players.has(attacker)) {
                    minigame.stats.getOrCreateStat(attacker, ArcadeStats.DAMAGE_DEALT).increment(amount)
                }
            }
        }
        scope.register<PlayerHealEvent>(phase = BuiltInEventPhases.POST) { event ->
            val (player, healAmount) = event
            minigame.stats.getOrCreateStat(player, ArcadeStats.DAMAGE_HEALED).increment(healAmount)
        }
    }

    override fun serialize(output: ValueOutput) {

    }

    override fun deserialize(input: ValueInput, version: Int) {

    }

    override fun type(): MinigameComponentType<*> {
        return TYPE
    }

    public companion object: MinigameComponentFactory {
        public val TYPE: MinigameComponentType<DefaultStatsComponent> = MinigameComponentType(arcade("default_stats"))

        override fun create(minigame: Minigame): MinigameComponent {
            return DefaultStatsComponent()
        }
    }
}
