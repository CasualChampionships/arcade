package net.casual.arcade.stats

import net.casual.arcade.Arcade

/**
 * This object contains the default stat types that
 * minigames will keep track of by default.
 */
public object ArcadeStats {
    public val RELOGS: StatType<Int> = StatType.int(Arcade.id("relogs"))
    public val KILLS: StatType<Int> = StatType.int(Arcade.id("kills"))
    public val DEATHS: StatType<Int> = StatType.int(Arcade.id("deaths"))
    public val DAMAGE_TAKEN: StatType<Float> = StatType.float(Arcade.id("damage_taken"))
    public val DAMAGE_DEALT: StatType<Float> = StatType.float(Arcade.id("damage_dealt"))
}