/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.level

import net.casual.arcade.utils.ServerUtils
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.gamerules.GameRule
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor
import net.minecraft.world.level.gamerules.GameRules

public fun GameRules.resetToDefault(server: MinecraftServer? = ServerUtils.getServerOrNull()) {
    this.visitGameRuleTypes(object: GameRuleTypeVisitor {
        override fun <T: Any> visit(rule: GameRule<T>) {
            set(rule, rule.defaultValue(), server)
        }
    })
}

public fun <T: Any> GameRules.set(rule: GameRule<T>, value: T) {
    this.set(rule, value, null)
}
