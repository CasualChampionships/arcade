package net.casual.arcade.utils

import net.casual.arcade.Arcade
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameRules.BooleanValue
import net.minecraft.world.level.GameRules.GameRuleTypeVisitor
import net.minecraft.world.level.GameRules.IntegerValue

public object GameRuleUtils {
    public fun GameRules.resetToDefault() {
        GameRules.visitGameRuleTypes(object: GameRuleTypeVisitor {
            override fun <T: GameRules.Value<T>> visit(key: GameRules.Key<T>, type: GameRules.Type<T>) {
                getRule(key).setFrom(type.createRule(), Arcade.getServer())
            }
        })
    }

    public fun GameRules.set(rule: GameRules.Key<BooleanValue>, value: Boolean) {
        this.getRule(rule).set(value, Arcade.getServer())
    }

    public fun GameRules.set(rule: GameRules.Key<IntegerValue>, value: Int) {
        this.getRule(rule).set(value, Arcade.getServer())
    }

    public fun GameRules.get(rule: GameRules.Key<BooleanValue>): Boolean {
        return this.getRule(rule).get()
    }

    public fun GameRules.get(rule: GameRules.Key<IntegerValue>): Int {
        return this.getRule(rule).get()
    }
}
