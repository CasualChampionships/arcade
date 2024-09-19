package net.casual.arcade.dimensions.vanilla

import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

public enum class VanillaDimension(public val key: ResourceKey<Level>) {
    Overworld(Level.OVERWORLD),
    Nether(Level.NETHER),
    End(Level.END)
}