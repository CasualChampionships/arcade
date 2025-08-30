/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import net.casual.arcade.minigame.stats.Stat
import net.casual.arcade.minigame.stats.StatTracker
import net.casual.arcade.minigame.stats.StatType
import net.minecraft.core.Holder
import net.minecraft.core.UUIDUtil
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

public class MinigameStatManager {
    private val stats = ConcurrentHashMap<UUID, StatTracker>()
    private var frozen = false

    public fun freeze() {
        this.frozen = true
        for (stat in this.stats.values) {
            stat.freeze()
        }
    }

    public fun unfreeze() {
        this.frozen = false
        for (stat in this.stats.values) {
            stat.unfreeze()
        }
    }

    public fun <T: Any> getOrCreateStat(player: ServerPlayer, type: Holder<StatType<T>>): Stat<T> {
        return this.getOrCreateStat(player.uuid, type)
    }

    public fun <T: Any> getOrCreateStat(uuid: UUID, type: Holder<StatType<T>>): Stat<T> {
        return this.getOrCreateTracker(uuid).getOrCreateStat(type)
    }

    public fun getOrCreateTracker(uuid: UUID): StatTracker {
        return this.stats.getOrPut(uuid) {
            StatTracker().also { if (this.frozen) it.freeze() }
        }
    }

    internal fun serialize(output: ValueOutput.ValueOutputList) {
        for ((uuid, tracker) in this.stats) {
            val child = output.addChild()
            child.store("uuid", UUIDUtil.STRING_CODEC, uuid)
            tracker.serialize(child.childrenList("stats"))
        }
    }

    internal fun deserialize(input: ValueInput.ValueInputList) {
        for (child in input) {
            val uuid = child.read("uuid", UUIDUtil.STRING_CODEC).getOrNull() ?: continue
            this.getOrCreateTracker(uuid).deserialize(child.childrenListOrEmpty("stats"))
        }
    }
}