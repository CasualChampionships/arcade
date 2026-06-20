/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.component.event

import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.NonExtendable

public fun interface ClickEventCallback {
    public fun click(player: ServerPlayer, payload: Tag?): Result

    public fun interface Payloadless: ClickEventCallback {
        public fun click(player: ServerPlayer): Result

        @NonExtendable
        override fun click(player: ServerPlayer, payload: Tag?): Result {
            return this.click(player)
        }
    }

    public enum class Result {
        Consume,
        Success,
        Fail
    }
}