package net.casual.arcade.utils

import net.casual.arcade.Arcade
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameRules.BooleanValue
import net.minecraft.world.level.GameRules.IntegerValue

public object GameRuleUtils {
    public fun GameRules.set(rule: GameRules.Key<BooleanValue>, value: Boolean) {
        this.getRule(rule).set(value, Arcade.getServer())
    }

    public fun GameRules.set(rule: GameRules.Key<IntegerValue>, value: Int) {
        this.getRule(rule).set(value, Arcade.getServer())
    }
}
