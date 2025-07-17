/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.stats

import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.core.Holder
import net.minecraft.core.Registry

/**
 * This object contains the default stat types that
 * minigames will keep track of by default.
 */
public object ArcadeStats {
    public val RELOGS: Holder.Reference<StatType<Int>> = this.register("relogs", StatType.int32())
    public val KILLS: Holder.Reference<StatType<Int>> = this.register("kills", StatType.int32())
    public val DEATHS: Holder.Reference<StatType<Int>> = this.register("deaths", StatType.int32())
    public val PLAY_TIME: Holder.Reference<StatType<Int>> = this.register("play_time", StatType.int32())
    public val DAMAGE_TAKEN: Holder.Reference<StatType<Float>> = this.register("damage_taken", StatType.float32())
    public val DAMAGE_DEALT: Holder.Reference<StatType<Float>> = this.register("damage_dealt", StatType.float32())
    public val DAMAGE_HEALED: Holder.Reference<StatType<Float>> = this.register("damage_healed", StatType.float32())

    internal fun load() {

    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> register(path: String, type: StatType<T>): Holder.Reference<StatType<T>> {
        val holder = Registry.registerForHolder(MinigameRegistries.STAT_TYPES, ArcadeUtils.id(path), type)
        return holder as Holder.Reference<StatType<T>>
    }
}