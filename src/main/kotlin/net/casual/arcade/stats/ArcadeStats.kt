package net.casual.arcade.stats

import net.casual.arcade.Arcade

/**
 * This object contains the default stat types that
 * minigames will keep track of by default.
 */
public object ArcadeStats {
    public val RELOGS: StatType<Int> = StatType.int32(Arcade.id("relogs"))
    public val KILLS: StatType<Int> = StatType.int32(Arcade.id("kills"))
    public val DEATHS: StatType<Int> = StatType.int32(Arcade.id("deaths"))
    public val PLAY_TIME: StatType<Int> = StatType.int32(Arcade.id("play_time"))
    public val DAMAGE_TAKEN: StatType<Float> = StatType.float32(Arcade.id("damage_taken"))
    public val DAMAGE_DEALT: StatType<Float> = StatType.float32(Arcade.id("damage_dealt"))
}