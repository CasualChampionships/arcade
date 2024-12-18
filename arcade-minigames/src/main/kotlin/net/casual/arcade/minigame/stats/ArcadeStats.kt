/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.stats

import net.casual.arcade.utils.ResourceUtils

/**
 * This object contains the default stat types that
 * minigames will keep track of by default.
 */
public object ArcadeStats {
    public val RELOGS: StatType<Int> = StatType.int32(ResourceUtils.arcade("relogs"))
    public val KILLS: StatType<Int> = StatType.int32(ResourceUtils.arcade("kills"))
    public val DEATHS: StatType<Int> = StatType.int32(ResourceUtils.arcade("deaths"))
    public val PLAY_TIME: StatType<Int> = StatType.int32(ResourceUtils.arcade("play_time"))
    public val DAMAGE_TAKEN: StatType<Float> = StatType.float32(ResourceUtils.arcade("damage_taken"))
    public val DAMAGE_DEALT: StatType<Float> = StatType.float32(ResourceUtils.arcade("damage_dealt"))
    public val DAMAGE_HEALED: StatType<Float> = StatType.float32(ResourceUtils.arcade("damage_healed"))
}