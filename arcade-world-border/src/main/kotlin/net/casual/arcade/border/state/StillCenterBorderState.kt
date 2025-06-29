/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.state

@Deprecated("This package is deprecated. Use 'net.casual.arcade.boundary' instead.")
public class StillCenterBorderState(
    private var centerX: Double,
    private var centerZ: Double
): CenterBorderState {
    override fun getCenterX(): Double {
        return this.centerX
    }

    override fun getCenterZ(): Double {
        return this.centerZ
    }

    override fun getTargetCenterX(): Double {
        return this.centerX
    }

    override fun getTargetCenterZ(): Double {
        return this.centerZ
    }

    override fun getLerpRemainingTime(): Long {
        return 0L
    }

    override fun update(): CenterBorderState {
        return this
    }

    override fun getStatus(): CenterBorderStatus {
        return CenterBorderStatus.STATIONARY
    }
}