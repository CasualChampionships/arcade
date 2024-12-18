/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.elements

import net.minecraft.world.BossEvent.BossBarOverlay

public object BossbarOverlayElements {
    private val PROGRESS = UniversalElement.constant(BossBarOverlay.PROGRESS)
    private val NOTCHED_6 = UniversalElement.constant(BossBarOverlay.NOTCHED_6)
    private val NOTCHED_10 = UniversalElement.constant(BossBarOverlay.NOTCHED_10)
    private val NOTCHED_12 = UniversalElement.constant(BossBarOverlay.NOTCHED_12)
    private val NOTCHED_20 = UniversalElement.constant(BossBarOverlay.NOTCHED_20)

    public fun of(overlay: BossBarOverlay): PlayerSpecificElement<BossBarOverlay> {
        return when (overlay) {
            BossBarOverlay.PROGRESS -> PROGRESS
            BossBarOverlay.NOTCHED_6 -> NOTCHED_6
            BossBarOverlay.NOTCHED_10 -> NOTCHED_10
            BossBarOverlay.NOTCHED_12 -> NOTCHED_12
            BossBarOverlay.NOTCHED_20 -> NOTCHED_20
        }
    }

    public fun progress(): PlayerSpecificElement<BossBarOverlay> {
        return PROGRESS
    }

    public fun notched6(): PlayerSpecificElement<BossBarOverlay> {
        return NOTCHED_6
    }

    public fun notched10(): PlayerSpecificElement<BossBarOverlay> {
        return NOTCHED_10
    }

    public fun notched12(): PlayerSpecificElement<BossBarOverlay> {
        return NOTCHED_12
    }

    public fun notched20(): PlayerSpecificElement<BossBarOverlay> {
        return NOTCHED_20
    }
}