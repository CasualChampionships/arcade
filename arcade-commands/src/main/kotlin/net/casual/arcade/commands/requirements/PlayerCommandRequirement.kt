/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.requirements

import net.casual.arcade.commands.player
import net.minecraft.commands.ServerPlayerSuggestionContext
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionSetSupplier
import java.util.function.Predicate

public class PlayerCommandRequirement<T: PermissionSetSupplier>(
    private val player: Predicate<ServerPlayer>,
    private val fallback: Predicate<PermissionSetSupplier>
): Predicate<T> {
    override fun test(context: T): Boolean {
        if (PLAYER_CONTEXT.isBound) {
            return this.player.test(PLAYER_CONTEXT.get())
        }
        if (context is ServerPlayerSuggestionContext) {
            return this.player.test(context.player)
        }
        return this.fallback.test(context)
    }

    public fun <S: PermissionSetSupplier> cast(): PlayerCommandRequirement<S> {
        @Suppress("UNCHECKED_CAST")
        return this as PlayerCommandRequirement<S>
    }

    public companion object {
        @JvmField
        internal val PLAYER_CONTEXT = ScopedValue.newInstance<ServerPlayer>()
    }
}