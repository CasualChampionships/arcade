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

@Deprecated("Use arcade's virtual entity implementation instead")
public abstract class PlayerSpecificDisplayElement: PlayerSpecificEntityElement() {
    public fun setTranslation(translation: Vector3fc) {
        val copy = Vector3f(translation)
        this.setDataEntry(DisplayTrackedData.TRANSLATION, copy)
    }

    public fun setTranslationFor(observer: ServerPlayer, translation: Vector3fc) {
        this.data.set(observer.uuid, DisplayTrackedData.TRANSLATION, Vector3f(translation))
    }

    public fun setTranslationToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.TRANSLATION)
    }

    public fun modifyTranslation(modifier: (Vector3fc) -> Vector3fc) {
        this.modifyDataEntry(DisplayTrackedData.TRANSLATION) { current -> Vector3f(modifier.invoke(current)) }
    }

    public fun setScale(scale: Vector3fc) {
        val copy = Vector3f(scale)
        this.setDataEntry(DisplayTrackedData.SCALE, copy)
    }

    public fun setScaleFor(observer: ServerPlayer, scale: Vector3fc) {
        this.data.set(observer.uuid, DisplayTrackedData.SCALE, Vector3f(scale))
    }

    public fun setScaleToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.SCALE)
    }

    public fun modifyScale(modifier: (Vector3fc) -> Vector3fc) {
        this.modifyDataEntry(DisplayTrackedData.SCALE) { current -> Vector3f(modifier.invoke(current)) }
    }

    public fun setLeftRotation(rotation: Quaternionfc) {
        val copy = Quaternionf(rotation)
        this.setDataEntry(DisplayTrackedData.LEFT_ROTATION, copy)
    }

    public fun setLeftRotationFor(observer: ServerPlayer, rotation: Quaternionfc) {
        this.data.set(observer.uuid, DisplayTrackedData.LEFT_ROTATION, Quaternionf(rotation))
    }

    public fun setLeftRotationToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.LEFT_ROTATION)
    }

    public fun modifyLeftRotation(modifier: (Quaternionfc) -> Quaternionfc) {
        this.modifyDataEntry(DisplayTrackedData.LEFT_ROTATION) { current -> Quaternionf(modifier.invoke(current)) }
    }

    public fun setRightRotation(rotation: Quaternionfc) {
        val copy = Quaternionf(rotation)
        this.setDataEntry(DisplayTrackedData.RIGHT_ROTATION, copy)
    }

    public fun setRightRotationFor(observer: ServerPlayer, rotation: Quaternionfc) {
        this.data.set(observer.uuid, DisplayTrackedData.RIGHT_ROTATION, Quaternionf(rotation))
    }

    public fun setRightRotationToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.RIGHT_ROTATION)
    }

    public fun modifyRightRotation(modifier: (Quaternionfc) -> Quaternionfc) {
        this.modifyDataEntry(DisplayTrackedData.RIGHT_ROTATION) { current -> Quaternionf(modifier.invoke(current)) }
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
        this.setDataEntry(DisplayTrackedData.INTERPOLATION_DURATION, ticks)
    }

    public fun setTransformationInterpolationFor(observer: ServerPlayer, ticks: Int) {
        this.setDataEntryFor(observer, DisplayTrackedData.INTERPOLATION_DURATION, ticks)
    }

    public fun setTeleportationInterpolation(ticks: Int) {
        this.setDataEntry(DisplayTrackedData.TELEPORTATION_DURATION, ticks)
    }

    public fun setTeleportationInterpolationFor(observer: ServerPlayer, ticks: Int) {
        this.setDataEntryFor(observer, DisplayTrackedData.TELEPORTATION_DURATION, ticks)
    }

    public fun setStartInterpolation(start: Int) {
        this.setDataEntry(DisplayTrackedData.START_INTERPOLATION, start)
    }

    public fun setStartInterpolationFor(observer: ServerPlayer, start: Int) {
        this.setDataEntryFor(observer, DisplayTrackedData.START_INTERPOLATION, start)
    }

    public fun startInterpolation() {
        this.data.modifyEntry(DisplayTrackedData.START_INTERPOLATION, true) { value -> value }
    }

    public fun startInterpolationFor(observer: ServerPlayer) {
        val previous = this.data.get(observer.uuid, DisplayTrackedData.START_INTERPOLATION) ?: return
        this.setDataEntryFor(observer, DisplayTrackedData.START_INTERPOLATION, previous)
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
        this.setDataEntry(DisplayTrackedData.BILLBOARD, byte)
    }

    public fun setBillboardConstraintsFor(observer: ServerPlayer, constraints: Display.BillboardConstraints) {
        val byte = constraints.ordinal.toByte()
        this.setDataEntryFor(observer, DisplayTrackedData.BILLBOARD, byte)
    }

    public fun setBillboardConstraintsToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.BILLBOARD)
    }

    public fun setBrightness(brightness: Brightness) {
        val packed = brightness.pack()
        this.setDataEntry(DisplayTrackedData.BRIGHTNESS, packed)
    }

    public fun setBrightnessFor(observer: ServerPlayer, brightness: Brightness) {
        val packed = brightness.pack()
        this.setDataEntryFor(observer, DisplayTrackedData.BRIGHTNESS, packed)
    }

    public fun setBrightnessToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.BRIGHTNESS)
    }

    public fun setViewRange(range: Float) {
        this.setDataEntry(DisplayTrackedData.VIEW_RANGE, range)
    }

    public fun setViewRangeFor(observer: ServerPlayer, range: Float) {
        this.setDataEntryFor(observer, DisplayTrackedData.VIEW_RANGE, range)
    }

    public fun setViewRangeToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.VIEW_RANGE)
    }

    public fun setShadowRadius(radius: Float) {
        this.setDataEntry(DisplayTrackedData.SHADOW_RADIUS, radius)
    }

    public fun setShadowRadiusFor(observer: ServerPlayer, radius: Float) {
        this.setDataEntryFor(observer, DisplayTrackedData.SHADOW_RADIUS, radius)
    }

    public fun setShadowRadiusToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.SHADOW_RADIUS)
    }

    public fun setShadowStrength(strength: Float) {
        this.setDataEntry(DisplayTrackedData.SHADOW_STRENGTH, strength)
    }

    public fun setShadowStrengthFor(observer: ServerPlayer, strength: Float) {
        this.setDataEntryFor(observer, DisplayTrackedData.SHADOW_STRENGTH, strength)
    }

    public fun setShadowStrengthToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.SHADOW_STRENGTH)
    }

    public fun setGlowColorOverride(color: Int) {
        this.setDataEntry(DisplayTrackedData.GLOW_COLOR_OVERRIDE, color)
    }

    public fun setGlowColorOverrideFor(observer: ServerPlayer, color: Int) {
        this.setDataEntryFor(observer, DisplayTrackedData.GLOW_COLOR_OVERRIDE, color)
    }

    public fun setGlowColorOverrideToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.GLOW_COLOR_OVERRIDE)
    }
}