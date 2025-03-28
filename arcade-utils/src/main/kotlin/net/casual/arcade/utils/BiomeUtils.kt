/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.minecraft.core.Holder
import net.minecraft.tags.BiomeTags
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.feature.Feature

public fun Holder<Biome>.isOcean(): Boolean {
    return this.isOf(BiomeTags.IS_OCEAN) || this.isOf(BiomeTags.IS_DEEP_OCEAN)
}

public fun Holder<Biome>.isOceanOrRiver(): Boolean {
    return this.isOcean() || this.isOf(BiomeTags.IS_RIVER)
}

public fun Biome.hasFeature(feature: Feature<*>): Boolean {
    val placedFeatures = this.generationSettings.features().stream().flatMap { it.stream() }
    for (placedFeature in placedFeatures) {
        if (placedFeature.value().features.anyMatch { it.feature() == feature }) {
            return true
        }
    }
    return false
}