/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.state

public interface CenterBorderState {
    public fun getCenterX(): Double

    public fun getCenterZ(): Double

    public fun getTargetCenterX(): Double

    public fun getTargetCenterZ(): Double

    public fun getLerpRemainingTime(): Long

    public fun update(): CenterBorderState

    public fun getStatus(): CenterBorderStatus
}