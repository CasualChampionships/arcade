/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.utils

import net.casual.arcade.virtual.entity.mixins.*
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.network.chat.Component
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.world.entity.Pose
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import org.joml.Quaternionfc
import org.joml.Vector3fc
import java.util.*

public object EntityDataAccessors {
    @JvmStatic
    @get:JvmName("getSharedFlags")
    public val SHARED_FLAGS: EntityDataAccessor<Byte>
        get() = EntityAccessor.accessSharedFlagsAccessor()

    @JvmStatic
    @get:JvmName("getAirSupply")
    public val AIR_SUPPLY: EntityDataAccessor<Int>
        get() = EntityAccessor.accessAirSupplyAccessor()

    @JvmStatic
    @get:JvmName("getCustomName")
    public val CUSTOM_NAME: EntityDataAccessor<Optional<Component>>
        get() = EntityAccessor.accessCustomNameAccessor()

    @JvmStatic
    @get:JvmName("getCustomNameVisible")
    public val CUSTOM_NAME_VISIBLE: EntityDataAccessor<Boolean>
        get() = EntityAccessor.accessCustomNameVisibleAccessor()

    @JvmStatic
    @get:JvmName("getSilent")
    public val SILENT: EntityDataAccessor<Boolean>
        get() = EntityAccessor.accessSilentAccessor()

    @JvmStatic
    @get:JvmName("getNoGravity")
    public val NO_GRAVITY: EntityDataAccessor<Boolean>
        get() = EntityAccessor.accessNoGravityAccessor()

    @JvmStatic
    @get:JvmName("getPose")
    public val POSE: EntityDataAccessor<Pose>
        get() = EntityAccessor.accessPoseAccessor()

    @JvmStatic
    @get:JvmName("getTicksFrozen")
    public val TICKS_FROZEN: EntityDataAccessor<Int>
        get() = EntityAccessor.accessTicksFrozenAccessor()

    public object LivingEntity {
        @JvmStatic
        @get:JvmName("getFlags")
        public val FLAGS: EntityDataAccessor<Byte>
            get() = LivingEntityAccessor.accessLivingEntityFlagsAccessor()

        @JvmStatic
        @get:JvmName("getHealth")
        public val HEALTH: EntityDataAccessor<Float>
            get() = LivingEntityAccessor.accessHealthAccessor()

        @JvmStatic
        @get:JvmName("getEffectParticles")
        public val EFFECT_PARTICLES: EntityDataAccessor<List<ParticleOptions>>
            get() = LivingEntityAccessor.accessEffectParticlesAccessor()

        @JvmStatic
        @get:JvmName("getEffectAmbience")
        public val EFFECT_AMBIENCE: EntityDataAccessor<Boolean>
            get() = LivingEntityAccessor.accessEffectAmbienceAccessor()

        @JvmStatic
        @get:JvmName("getArrowCount")
        public val ARROW_COUNT: EntityDataAccessor<Int>
            get() = LivingEntityAccessor.accessArrowCountAccessor()

        @JvmStatic
        @get:JvmName("getStingerCount")
        public val STINGER_COUNT: EntityDataAccessor<Int>
            get() = LivingEntityAccessor.accessStingerCountAccessor()

        @JvmStatic
        @get:JvmName("getSleepingPos")
        public val SLEEPING_POS: EntityDataAccessor<Optional<BlockPos>>
            get() = LivingEntityAccessor.accessSleepingPosAccessor()
    }

    public object Display {
        @JvmStatic
        @get:JvmName("getTransformationInterpolationStartDeltaTicks")
        public val TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS: EntityDataAccessor<Int>
            get() = DisplayAccessor.accessTransformationInterpolationStartDeltaTicksAccessor()

        @JvmStatic
        @get:JvmName("getTransformationInterpolationDuration")
        public val TRANSFORMATION_INTERPOLATION_DURATION: EntityDataAccessor<Int>
            get() = DisplayAccessor.accessTransformationInterpolationDurationAccessor()

        @JvmStatic
        @get:JvmName("getPosRotInterpolationDuration")
        public val POS_ROT_INTERPOLATION_DURATION: EntityDataAccessor<Int>
            get() = DisplayAccessor.accessPosRotInterpolationDurationAccessor()

        @JvmStatic
        @get:JvmName("getTranslation")
        public val TRANSLATION: EntityDataAccessor<Vector3fc>
            get() = DisplayAccessor.accessTranslationAccessor()

        @JvmStatic
        @get:JvmName("getScale")
        public val SCALE: EntityDataAccessor<Vector3fc>
            get() = DisplayAccessor.accessScaleAccessor()

        @JvmStatic
        @get:JvmName("getLeftRotation")
        public val LEFT_ROTATION: EntityDataAccessor<Quaternionfc>
            get() = DisplayAccessor.accessLeftRotationAccessor()

        @JvmStatic
        @get:JvmName("getRightRotation")
        public val RIGHT_ROTATION: EntityDataAccessor<Quaternionfc>
            get() = DisplayAccessor.accessRightRotationAccessor()

        @JvmStatic
        @get:JvmName("getBillboardRenderConstraints")
        public val BILLBOARD_RENDER_CONSTRAINTS: EntityDataAccessor<Byte>
            get() = DisplayAccessor.accessBillboardRenderConstraintsAccessor()

        @JvmStatic
        @get:JvmName("getBrightnessOverride")
        public val BRIGHTNESS_OVERRIDE: EntityDataAccessor<Int>
            get() = DisplayAccessor.accessBrightnessOverrideAccessor()

        @JvmStatic
        @get:JvmName("getViewRange")
        public val VIEW_RANGE: EntityDataAccessor<Float>
            get() = DisplayAccessor.accessViewRangeAccessor()

        @JvmStatic
        @get:JvmName("getShadowRadius")
        public val SHADOW_RADIUS: EntityDataAccessor<Float>
            get() = DisplayAccessor.accessShadowRadiusAccessor()

        @JvmStatic
        @get:JvmName("getShadowStrength")
        public val SHADOW_STRENGTH: EntityDataAccessor<Float>
            get() = DisplayAccessor.accessShadowStrengthAccessor()

        @JvmStatic
        @get:JvmName("getWidth")
        public val WIDTH: EntityDataAccessor<Float>
            get() = DisplayAccessor.accessWidthAccessor()

        @JvmStatic
        @get:JvmName("getHeight")
        public val HEIGHT: EntityDataAccessor<Float>
            get() = DisplayAccessor.accessHeightAccessor()

        @JvmStatic
        @get:JvmName("getGlowColorOverride")
        public val GLOW_COLOR_OVERRIDE: EntityDataAccessor<Int>
            get() = DisplayAccessor.accessGlowColorOverrideAccessor()

        public object Item {
            @JvmStatic
            @get:JvmName("getItemStack")
            public val ITEM_STACK: EntityDataAccessor<ItemStack>
                get() = ItemDisplayAccessor.accessItemStackAccessor()

            @JvmStatic
            @get:JvmName("getItemDisplayContext")
            public val ITEM_DISPLAY_CONTEXT: EntityDataAccessor<Byte>
                get() = ItemDisplayAccessor.accessItemDisplayAccessor()
        }

        public object Block {
            @JvmStatic
            @get:JvmName("getBlockState")
            public val BLOCK_STATE: EntityDataAccessor<BlockState>
                get() = BlockDisplayAccessor.accessBlockStateAccessor()
        }

        public object Text {
            @JvmStatic
            @get:JvmName("getText")
            public val TEXT: EntityDataAccessor<Component>
                get() = TextDisplayAccessor.accessTextAccessor()

            @JvmStatic
            @get:JvmName("getLineWidth")
            public val LINE_WIDTH: EntityDataAccessor<Int>
                get() = TextDisplayAccessor.accessLineWidthAccessor()

            @JvmStatic
            @get:JvmName("getBackgroundColor")
            public val BACKGROUND_COLOR: EntityDataAccessor<Int>
                get() = TextDisplayAccessor.accessBackgroundColorAccessor()

            @JvmStatic
            @get:JvmName("getTextOpacity")
            public val TEXT_OPACITY: EntityDataAccessor<Byte>
                get() = TextDisplayAccessor.accessTextOpacityAccessor()

            @JvmStatic
            @get:JvmName("getStyleFlags")
            public val STYLE_FLAGS: EntityDataAccessor<Byte>
                get() = TextDisplayAccessor.accessStyleFlagsAccessor()
        }
    }
}