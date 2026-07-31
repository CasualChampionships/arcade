/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler

import net.casual.arcade.scheduler.utils.TaskRegistries
import net.fabricmc.api.ModInitializer
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
public object ArcadeScheduler: ModInitializer {
    override fun onInitialize() {
        TaskRegistries.load()
        GlobalTickedScheduler.loadServer()
    }
}