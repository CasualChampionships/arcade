/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.display

import com.mojang.math.Transformation
import net.casual.arcade.virtual.entity.SimpleVirtualEntity
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Brightness
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import org.joml.Quaternionf
import org.joml.Quaternionfc
import org.joml.Vector3f
import org.joml.Vector3fc
import net.casual.arcade.virtual.entity.utils.EntityDataAccessors.Display as DisplayDataAccessors

public abstract class SimpleVirtualDisplay(
    type: EntityType<out Display>,
    attachment: VirtualEntityAttachment
): SimpleVirtualEntity(type, attachment) {
    public fun setTranslation(translation: Vector3fc) {
        val copy = Vector3f(translation)
        this.data.modifyEntry(DisplayDataAccessors.TRANSLATION) { copy }
    }

    public fun setTranslationFor(observer: ServerPlayer, translation: Vector3fc) {
        this.data.set(observer.uuid, DisplayDataAccessors.TRANSLATION, Vector3f(translation))
    }

    public fun setTranslationToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, DisplayDataAccessors.TRANSLATION)
    }

    public fun modifyTranslation(modifier: (Vector3fc) -> Vector3fc) {
        this.data.modifyEntry(DisplayDataAccessors.TRANSLATION, false) { current -> Vector3f(modifier.invoke(current)) }
    }

    public fun setScale(scale: Vector3fc) {
        val copy = Vector3f(scale)
        this.data.modifyEntry(DisplayDataAccessors.SCALE) { copy }
    }

    public fun setScaleFor(observer: ServerPlayer, scale: Vector3fc) {
        this.data.set(observer.uuid, DisplayDataAccessors.SCALE, Vector3f(scale))
    }

    public fun setScaleToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, DisplayDataAccessors.SCALE)
    }

    public fun modifyScale(modifier: (Vector3fc) -> Vector3fc) {
        this.data.modifyEntry(DisplayDataAccessors.SCALE, false) { current -> Vector3f(modifier.invoke(current)) }
    }

    public fun setLeftRotation(rotation: Quaternionfc) {
        val copy = Quaternionf(rotation)
        this.data.modifyEntry(DisplayDataAccessors.LEFT_ROTATION) { copy }
    }

    public fun setLeftRotationFor(observer: ServerPlayer, rotation: Quaternionfc) {
        this.data.set(observer.uuid, DisplayDataAccessors.LEFT_ROTATION, Quaternionf(rotation))
    }

    public fun setLeftRotationToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, DisplayDataAccessors.LEFT_ROTATION)
    }

    public fun modifyLeftRotation(modifier: (Quaternionfc) -> Quaternionfc) {
        this.data.modifyEntry(DisplayDataAccessors.LEFT_ROTATION, false) { current -> Quaternionf(modifier.invoke(current)) }
    }

    public fun setRightRotation(rotation: Quaternionfc) {
        val copy = Quaternionf(rotation)
        this.data.modifyEntry(DisplayDataAccessors.RIGHT_ROTATION) { copy }
    }

    public fun setRightRotationFor(observer: ServerPlayer, rotation: Quaternionfc) {
        this.data.set(observer.uuid, DisplayDataAccessors.RIGHT_ROTATION, Quaternionf(rotation))
    }

    public fun setRightRotationToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, DisplayDataAccessors.RIGHT_ROTATION)
    }

    public fun modifyRightRotation(modifier: (Quaternionfc) -> Quaternionfc) {
        this.data.modifyEntry(DisplayDataAccessors.RIGHT_ROTATION, false) { current -> Quaternionf(modifier.invoke(current)) }
    }

    public fun setTransformation(transformation: Transformation) {
        this.data.modifyEntry(DisplayDataAccessors.TRANSLATION) { transformation.translation }
        this.data.modifyEntry(DisplayDataAccessors.LEFT_ROTATION) { transformation.leftRotation }
        this.data.modifyEntry(DisplayDataAccessors.SCALE) { transformation.scale }
        this.data.modifyEntry(DisplayDataAccessors.RIGHT_ROTATION) { transformation.rightRotation }
    }

    public fun setTransformationFor(observer: ServerPlayer, transformation: Transformation) {
        this.data.set(observer.uuid, DisplayDataAccessors.TRANSLATION, transformation.translation)
        this.data.set(observer.uuid, DisplayDataAccessors.LEFT_ROTATION, transformation.leftRotation)
        this.data.set(observer.uuid, DisplayDataAccessors.SCALE, transformation.scale)
        this.data.set(observer.uuid, DisplayDataAccessors.RIGHT_ROTATION, transformation.rightRotation)
    }

    public fun isTransformationDirtyFor(observer: ServerPlayer): Boolean {
        return this.data.isDirty(observer.uuid, DisplayDataAccessors.TRANSLATION)
            || this.data.isDirty(observer.uuid, DisplayDataAccessors.LEFT_ROTATION)
            || this.data.isDirty(observer.uuid, DisplayDataAccessors.SCALE)
            || this.data.isDirty(observer.uuid, DisplayDataAccessors.RIGHT_ROTATION)
    }

    public fun setTransformationInterpolation(ticks: Int) {
        this.data.modifyEntry(DisplayDataAccessors.TRANSFORMATION_INTERPOLATION_DURATION) { ticks }
    }

    public fun setTransformationInterpolationFor(observer: ServerPlayer, ticks: Int) {
        this.data.set(observer.uuid, DisplayDataAccessors.TRANSFORMATION_INTERPOLATION_DURATION, ticks)
    }

    public fun setTeleportationInterpolation(ticks: Int) {
        this.data.modifyEntry(DisplayDataAccessors.POS_ROT_INTERPOLATION_DURATION) { ticks }
    }

    public fun setTeleportationInterpolationFor(observer: ServerPlayer, ticks: Int) {
        this.data.set(observer.uuid, DisplayDataAccessors.POS_ROT_INTERPOLATION_DURATION, ticks)
    }

    public fun setStartInterpolation(start: Int) {
        this.data.modifyEntry(DisplayDataAccessors.TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS) { start }
    }

    public fun setStartInterpolationFor(observer: ServerPlayer, start: Int) {
        this.data.set(observer.uuid, DisplayDataAccessors.TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS, start)
    }

    public fun startInterpolation() {
        this.data.modifyEntry(DisplayDataAccessors.TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS, true) { value -> value }
    }

    public fun startInterpolationFor(observer: ServerPlayer) {
        val previous = this.data.get(observer.uuid, DisplayDataAccessors.TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS) ?: return
        this.data.set(observer.uuid, DisplayDataAccessors.TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS, previous)
    }

    public fun startInterpolationIfDirty() {
        for (connection in this.connections) {
            val observer = connection.player
            if (this.isTransformationDirtyFor(observer)) {
                this.startInterpolationFor(observer)
            }
        }
    }

    public fun setBillboardConstraints(constraints: Display.BillboardConstraints) {
        val byte = constraints.ordinal.toByte()
        this.data.modifyEntry(DisplayDataAccessors.BILLBOARD_RENDER_CONSTRAINTS) { byte }
    }

    public fun setBillboardConstraintsFor(observer: ServerPlayer, constraints: Display.BillboardConstraints) {
        val byte = constraints.ordinal.toByte()
        this.data.set(observer.uuid, DisplayDataAccessors.BILLBOARD_RENDER_CONSTRAINTS, byte)
    }

    public fun setBillboardConstraintsToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, DisplayDataAccessors.BILLBOARD_RENDER_CONSTRAINTS)
    }

    public fun setBrightness(brightness: Brightness) {
        val packed = brightness.pack()
        this.data.modifyEntry(DisplayDataAccessors.BRIGHTNESS_OVERRIDE) { packed }
    }

    public fun setBrightnessFor(observer: ServerPlayer, brightness: Brightness) {
        val packed = brightness.pack()
        this.data.set(observer.uuid, DisplayDataAccessors.BRIGHTNESS_OVERRIDE, packed)
    }

    public fun setBrightnessToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, DisplayDataAccessors.BRIGHTNESS_OVERRIDE)
    }

    public fun setViewRange(range: Float) {
        this.data.modifyEntry(DisplayDataAccessors.VIEW_RANGE) { range }
    }

    public fun setViewRangeFor(observer: ServerPlayer, range: Float) {
        this.data.set(observer.uuid, DisplayDataAccessors.VIEW_RANGE, range)
    }

    public fun setViewRangeToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, DisplayDataAccessors.VIEW_RANGE)
    }

    public fun setShadowRadius(radius: Float) {
        this.data.modifyEntry(DisplayDataAccessors.SHADOW_RADIUS) { radius }
    }

    public fun setShadowRadiusFor(observer: ServerPlayer, radius: Float) {
        this.data.set(observer.uuid, DisplayDataAccessors.SHADOW_RADIUS, radius)
    }

    public fun setShadowRadiusToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, DisplayDataAccessors.SHADOW_RADIUS)
    }

    public fun setShadowStrength(strength: Float) {
        this.data.modifyEntry(DisplayDataAccessors.SHADOW_STRENGTH) { strength }
    }

    public fun setShadowStrengthFor(observer: ServerPlayer, strength: Float) {
        this.data.set(observer.uuid, DisplayDataAccessors.SHADOW_STRENGTH, strength)
    }

    public fun setShadowStrengthToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, DisplayDataAccessors.SHADOW_STRENGTH)
    }

    public fun setGlowColorOverride(color: Int) {
        this.data.modifyEntry(DisplayDataAccessors.GLOW_COLOR_OVERRIDE) { color }
    }

    public fun setGlowColorOverrideFor(observer: ServerPlayer, color: Int) {
        this.data.set(observer.uuid, DisplayDataAccessors.GLOW_COLOR_OVERRIDE, color)
    }

    public fun setGlowColorOverrideToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, DisplayDataAccessors.GLOW_COLOR_OVERRIDE)
    }
}