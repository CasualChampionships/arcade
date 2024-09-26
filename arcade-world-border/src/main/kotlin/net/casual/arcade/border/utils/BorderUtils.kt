package net.casual.arcade.border.utils

import net.casual.arcade.border.ducks.BorderSetter
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.border.WorldBorder

public fun ServerLevel.setWorldBorder(border: WorldBorder) {
    (this as BorderSetter).`arcade$setBorder`(border)
}