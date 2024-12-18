/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameRules.*

public fun GameRules.resetToDefault(server: MinecraftServer? = ServerUtils.getServerOrNull()) {
    visitGameRuleTypes(object: GameRuleTypeVisitor {
        override fun <T: Value<T>> visit(key: Key<T>, type: Type<T>) {
            getRule(key).setFrom(type.createRule(), server)
        }
    })
}

public fun GameRules.set(
    rule: Key<BooleanValue>,
    value: Boolean,
    server: MinecraftServer? = ServerUtils.getServerOrNull()
) {
    this.getRule(rule).set(value, server)
}

public fun GameRules.set(
    rule: Key<IntegerValue>,
    value: Int,
    server: MinecraftServer? = ServerUtils.getServerOrNull()
) {
    this.getRule(rule).set(value, server)
}

public fun GameRules.get(rule: Key<BooleanValue>): Boolean {
    return this.getRule(rule).get()
}

public fun GameRules.get(rule: Key<IntegerValue>): Int {
    return this.getRule(rule).get()
}
