/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.utils

import net.casual.arcade.border.ducks.BorderSetter
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.border.WorldBorder

public fun ServerLevel.setWorldBorder(border: WorldBorder) {
    (this as BorderSetter).`arcade$setBorder`(border)
}