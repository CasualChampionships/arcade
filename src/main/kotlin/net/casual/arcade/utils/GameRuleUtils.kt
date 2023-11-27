package net.casual.arcade.utils

import net.casual.arcade.Arcade
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameRules.*

public object GameRuleUtils {
    public fun GameRules.resetToDefault() {
        visitGameRuleTypes(object: GameRuleTypeVisitor {
            override fun <T: Value<T>> visit(key: Key<T>, type: Type<T>) {
                getRule(key).setFrom(type.createRule(), Arcade.getServer())
            }
        })
    }

    public fun GameRules.set(rule: Key<BooleanValue>, value: Boolean) {
        this.getRule(rule).set(value, Arcade.getServer())
    }

    public fun GameRules.set(rule: Key<IntegerValue>, value: Int) {
        this.getRule(rule).set(value, Arcade.getServer())
    }

    public fun GameRules.get(rule: Key<BooleanValue>): Boolean {
        return this.getRule(rule).get()
    }

    public fun GameRules.get(rule: Key<IntegerValue>): Int {
        return this.getRule(rule).get()
    }
}
