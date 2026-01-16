/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.element

import com.mojang.math.Transformation
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Brightness
import net.minecraft.world.entity.Display
import org.joml.Quaternionf
import org.joml.Quaternionfc
import org.joml.Vector3f
import org.joml.Vector3fc

public abstract class PlayerSpecificDisplayElement: PlayerSpecificEntityElement() {
    public fun setTranslation(translation: Vector3fc) {
        val copy = Vector3f(translation)
        this.data.modifyEntry(DisplayTrackedData.TRANSLATION) { copy }
    }

    public fun setTranslationFor(observer: ServerPlayer, translation: Vector3fc) {
        this.data.set(observer.uuid, DisplayTrackedData.TRANSLATION, Vector3f(translation))
    }

    public fun setScale(scale: Vector3fc) {
        val copy = Vector3f(scale)
        this.data.modifyEntry(DisplayTrackedData.SCALE) { copy }
    }

    public fun setScaleFor(observer: ServerPlayer, scale: Vector3fc) {
        this.data.set(observer.uuid, DisplayTrackedData.SCALE, Vector3f(scale))
    }

    public fun setLeftRotation(rotation: Quaternionfc) {
        val copy = Quaternionf(rotation)
        this.data.modifyEntry(DisplayTrackedData.LEFT_ROTATION) { copy }
    }

    public fun setLeftRotationFor(observer: ServerPlayer, rotation: Quaternionfc) {
        this.data.set(observer.uuid, DisplayTrackedData.LEFT_ROTATION, Quaternionf(rotation))
    }

    public fun setRightRotation(rotation: Quaternionfc) {
        val copy = Quaternionf(rotation)
        this.data.modifyEntry(DisplayTrackedData.RIGHT_ROTATION) { copy }
    }

    public fun setRightRotationFor(observer: ServerPlayer, rotation: Quaternionfc) {
        this.data.set(observer.uuid, DisplayTrackedData.RIGHT_ROTATION, Quaternionf(rotation))
    }

    public fun setTransformation(transformation: Transformation) {
        this.data.modifyEntry(DisplayTrackedData.TRANSLATION) { transformation.translation }
        this.data.modifyEntry(DisplayTrackedData.LEFT_ROTATION) { transformation.leftRotation }
        this.data.modifyEntry(DisplayTrackedData.SCALE) { transformation.scale }
        this.data.modifyEntry(DisplayTrackedData.RIGHT_ROTATION) { transformation.rightRotation }
    }

    public fun setTransformationFor(observer: ServerPlayer, transformation: Transformation) {
        this.data.set(observer.uuid, DisplayTrackedData.TRANSLATION, transformation.translation)
        this.data.set(observer.uuid, DisplayTrackedData.LEFT_ROTATION, transformation.leftRotation)
        this.data.set(observer.uuid, DisplayTrackedData.SCALE, transformation.scale)
        this.data.set(observer.uuid, DisplayTrackedData.RIGHT_ROTATION, transformation.rightRotation)
    }

    public fun isTransformationDirtyFor(observer: ServerPlayer): Boolean {
        return this.data.isDirty(observer.uuid, DisplayTrackedData.TRANSLATION)
            || this.data.isDirty(observer.uuid, DisplayTrackedData.LEFT_ROTATION)
            || this.data.isDirty(observer.uuid, DisplayTrackedData.SCALE)
            || this.data.isDirty(observer.uuid, DisplayTrackedData.RIGHT_ROTATION)
    }

    public fun setTransformationInterpolation(ticks: Int) {
        this.data.modifyEntry(DisplayTrackedData.INTERPOLATION_DURATION) { ticks }
    }

    public fun setTransformationInterpolationFor(observer: ServerPlayer, ticks: Int) {
        this.data.set(observer.uuid, DisplayTrackedData.INTERPOLATION_DURATION, ticks)
    }

    public fun setTeleportationInterpolation(ticks: Int) {
        this.data.modifyEntry(DisplayTrackedData.TELEPORTATION_DURATION) { ticks }
    }

    public fun setTeleportationInterpolationFor(observer: ServerPlayer, ticks: Int) {
        this.data.set(observer.uuid, DisplayTrackedData.TELEPORTATION_DURATION, ticks)
    }

    public fun setStartInterpolation(start: Int) {
        this.data.modifyEntry(DisplayTrackedData.START_INTERPOLATION) { start }
    }

    public fun setStartInterpolationFor(observer: ServerPlayer, start: Int) {
        this.data.set(observer.uuid, DisplayTrackedData.START_INTERPOLATION, start)
    }

    public fun startInterpolation() {
        this.data.modifyEntry(DisplayTrackedData.START_INTERPOLATION, true) { value -> value }
    }

    public fun startInterpolationFor(observer: ServerPlayer) {
        val previous = this.data.get(observer.uuid, DisplayTrackedData.START_INTERPOLATION) ?: return
        this.data.set(observer.uuid, DisplayTrackedData.START_INTERPOLATION, previous)
    }

    public fun startInterpolationIfDirty() {
        val holder = this.holder ?: return
        for (connection in holder.watchingPlayers) {
            val observer = connection.player
            if (this.isTransformationDirtyFor(observer)) {
                this.startInterpolationFor(observer)
            }
        }
    }

    public fun setBillboardConstraints(constraints: Display.BillboardConstraints) {
        val byte = constraints.ordinal.toByte()
        this.data.modifyEntry(DisplayTrackedData.BILLBOARD) { byte }
    }

    public fun setBillboardConstraintsFor(observer: ServerPlayer, constraints: Display.BillboardConstraints) {
        val byte = constraints.ordinal.toByte()
        this.data.set(observer.uuid, DisplayTrackedData.BILLBOARD, byte)
    }

    public fun setBrightness(brightness: Brightness) {
        val packed = brightness.pack()
        this.data.modifyEntry(DisplayTrackedData.BRIGHTNESS) { packed }
    }

    public fun setBrightnessFor(observer: ServerPlayer, brightness: Brightness) {
        val packed = brightness.pack()
        this.data.set(observer.uuid, DisplayTrackedData.BRIGHTNESS, packed)
    }

    public fun setViewRange(range: Float) {
        this.data.modifyEntry(DisplayTrackedData.VIEW_RANGE) { range }
    }

    public fun setViewRangeFor(observer: ServerPlayer, range: Float) {
        this.data.set(observer.uuid, DisplayTrackedData.VIEW_RANGE, range)
    }

    public fun setShadowRadius(radius: Float) {
        this.data.modifyEntry(DisplayTrackedData.SHADOW_RADIUS) { radius }
    }

    public fun setShadowRadiusFor(observer: ServerPlayer, radius: Float) {
        this.data.set(observer.uuid, DisplayTrackedData.SHADOW_RADIUS, radius)
    }

    public fun setShadowStrength(strength: Float) {
        this.data.modifyEntry(DisplayTrackedData.SHADOW_STRENGTH) { strength }
    }

    public fun setShadowStrengthFor(observer: ServerPlayer, strength: Float) {
        this.data.set(observer.uuid, DisplayTrackedData.SHADOW_STRENGTH, strength)
    }

    public fun setGlowColorOverride(color: Int) {
        this.data.modifyEntry(DisplayTrackedData.GLOW_COLOR_OVERRIDE) { color }
    }

    public fun setGlowColorOverrideFor(observer: ServerPlayer, color: Int) {
        this.data.set(observer.uuid, DisplayTrackedData.GLOW_COLOR_OVERRIDE, color)
    }
}