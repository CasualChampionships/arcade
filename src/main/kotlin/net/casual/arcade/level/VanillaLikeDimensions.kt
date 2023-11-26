package net.casual.arcade.level

import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

public data class VanillaLikeDimensions(
    var overworld: ResourceKey<Level>? = null,
    var nether: ResourceKey<Level>? = null,
    var end: ResourceKey<Level>? = null
)