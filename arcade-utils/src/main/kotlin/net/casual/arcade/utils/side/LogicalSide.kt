/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.side

import net.casual.arcade.utils.server.ServerSingleton
import net.minecraft.client.Minecraft
import net.minecraft.util.thread.ReentrantBlockableEventLoop

/**
 * This represents one of the two logical sides, each of which owns
 * a main thread and an executor that runs work on it.
 *
 * This is used to determine which thread a given piece of work
 * belongs to, and whether the caller is already on it, for example
 * which thread a scheduler ticks on, or which thread an event must
 * be broadcast on.
 */
public enum class LogicalSide {
    Server {
        override fun executor(): ReentrantBlockableEventLoop<*>? {
            return ServerSingleton.getOrNull()
        }
    },
    Client {
        override fun executor(): ReentrantBlockableEventLoop<*> {
            return Minecraft.getInstance()
        }
    };

    /**
     * The main-thread executor for this side, or `null` if it
     * does not currently exist.
     */
    public abstract fun executor(): ReentrantBlockableEventLoop<*>?

    /**
     * Whether the calling thread is this side's main thread.
     *
     * @return `true` if the caller is on this side's main thread.
     */
    public fun isOnThread(): Boolean {
        return this.executor()?.isSameThread ?: false
    }
}
