package net.casual.arcade.utils

import net.casual.arcade.Arcade
import net.minecraft.world.level.GameRules

public object GameRuleUtils {
    public fun <T: GameRules.Value<T>> GameRules.set(rule: GameRules.Key<T>, value: T) {
        this.getRule(rule).setFrom(value, Arcade.getServer())
    }
}