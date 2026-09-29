/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.utils.boundary

import net.casual.arcade.boundary.LevelBoundary
import net.casual.arcade.boundary.utils.levelBoundary
import net.casual.arcade.gametest.TestContext
import net.minecraft.server.level.ServerLevel

public fun TestContext.boundary(level: ServerLevel): LevelBoundary {
    return this.assertNotNull(level.levelBoundary, "Level ${level.dimension().identifier()} has no boundary")
}