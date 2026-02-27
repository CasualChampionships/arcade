/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.class)
public interface DisplayAccessor {
    @Accessor("DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID")
    static EntityDataAccessor<Integer> accessTransformationInterpolationStartDeltaTicksAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID")
    static EntityDataAccessor<Integer> accessTransformationInterpolationDurationAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_POS_ROT_INTERPOLATION_DURATION_ID")
    static EntityDataAccessor<Integer> accessPosRotInterpolationDurationAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_TRANSLATION_ID")
    static EntityDataAccessor<Vector3fc> accessTranslationAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_SCALE_ID")
    static EntityDataAccessor<Vector3fc> accessScaleAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_LEFT_ROTATION_ID")
    static EntityDataAccessor<Quaternionfc> accessLeftRotationAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_RIGHT_ROTATION_ID")
    static EntityDataAccessor<Quaternionfc> accessRightRotationAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_BILLBOARD_RENDER_CONSTRAINTS_ID")
    static EntityDataAccessor<Byte> accessBillboardRenderConstraintsAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_BRIGHTNESS_OVERRIDE_ID")
    static EntityDataAccessor<Integer> accessBrightnessOverrideAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_VIEW_RANGE_ID")
    static EntityDataAccessor<Float> accessViewRangeAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_SHADOW_RADIUS_ID")
    static EntityDataAccessor<Float> accessShadowRadiusAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_SHADOW_STRENGTH_ID")
    static EntityDataAccessor<Float> accessShadowStrengthAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_WIDTH_ID")
    static EntityDataAccessor<Float> accessWidthAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_HEIGHT_ID")
    static EntityDataAccessor<Float> accessHeightAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_GLOW_COLOR_OVERRIDE_ID")
    static EntityDataAccessor<Integer> accessGlowColorOverrideAccessor() {
        throw new AssertionError();
    }
}
