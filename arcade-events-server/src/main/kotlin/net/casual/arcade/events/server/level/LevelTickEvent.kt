/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.level

import net.minecraft.server.level.ServerLevel

public data class LevelTickEvent(
    override val level: ServerLevel
): LevelEvent