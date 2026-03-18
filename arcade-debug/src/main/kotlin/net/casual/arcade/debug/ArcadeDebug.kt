/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.debug

import net.casual.arcade.debug.behavior.BehaviorDescriptionOverrides
import net.fabricmc.api.ModInitializer

public object ArcadeDebug: ModInitializer {
    override fun onInitialize() {
        BehaviorDescriptionOverrides.bootstrap()
    }
}