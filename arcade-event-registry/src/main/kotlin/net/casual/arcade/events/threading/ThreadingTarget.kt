/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.threading

import net.casual.arcade.events.threading.ThreadingStrategy.Option

public enum class ThreadingTarget(strategy: ThreadingStrategy): ThreadingStrategy by strategy {
    Default(ThreadingStrategy { if (it is AsyncEvent) Option.UseCurrentThread else Option.ForceMainThread }),
    ForceMainThread(ThreadingStrategy { Option.ForceMainThread }),
    UseCurrentThread(ThreadingStrategy { Option.UseCurrentThread })
}