/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server

import net.casual.arcade.events.common.ServerSideEvent
import net.minecraft.server.MinecraftServer
import org.jetbrains.annotations.ApiStatus.Internal

public data class ServerSaveEvent(
    val server: MinecraftServer,
    val reason: Reason
): ServerSideEvent {
    @Deprecated("Manually check reason instead")
    val stopping: Boolean
        get() = this.reason == Reason.Shutdown

    val isRoutine: Boolean
        get() = this.reason != Reason.Initial && this.reason != Reason.Shutdown

    public enum class Reason {
        Initial, AutoSave, Manual, Shutdown
    }

    @Internal
    public companion object {
        @Internal @JvmField public val SAVE_REASON: ScopedValue<Reason> = ScopedValue.newInstance()

        @Internal
        @JvmStatic
        public fun createWithContextualReason(server: MinecraftServer): ServerSaveEvent {
            val reason = if (SAVE_REASON.isBound) SAVE_REASON.get() else Reason.AutoSave
            return ServerSaveEvent(server, reason)
        }
    }
}