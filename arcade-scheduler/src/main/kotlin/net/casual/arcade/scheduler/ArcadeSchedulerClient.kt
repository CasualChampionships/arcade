/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler

import net.fabricmc.api.ClientModInitializer
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
public object ArcadeSchedulerClient: ClientModInitializer {
    override fun onInitializeClient() {
        GlobalTickedScheduler.loadClient()
    }
}
