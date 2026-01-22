/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.utils

import net.casual.arcade.virtual.entity.mixins.BlockDisplayAccessor
import net.casual.arcade.virtual.entity.mixins.DisplayAccessor
import net.casual.arcade.virtual.entity.mixins.EntityAccessor
import net.casual.arcade.virtual.entity.mixins.ItemDisplayAccessor
import net.casual.arcade.virtual.entity.mixins.TextDisplayAccessor
import net.minecraft.network.chat.Component
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.world.entity.Pose
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import org.apache.commons.codec.binary.Hex
import org.joml.Quaternionfc
import org.joml.Vector3fc
import java.util.Optional

public object EntityDataAccessors {
    public val SHARED_FLAGS: EntityDataAccessor<Byte>
        get() = EntityAccessor.accessSharedFlagsAccessor()

    public val AIR_SUPPLY: EntityDataAccessor<Int>
        get() = EntityAccessor.accessAirSupplyAccessor()

    public val CUSTOM_NAME: EntityDataAccessor<Optional<Component>>
        get() = EntityAccessor.accessCustomNameAccessor()

    public val CUSTOM_NAME_VISIBLE: EntityDataAccessor<Boolean>
        get() = EntityAccessor.accessCustomNameVisibleAccessor()

    public val SILENT: EntityDataAccessor<Boolean>
        get() = EntityAccessor.accessSilentAccessor()

    public val NO_GRAVITY: EntityDataAccessor<Boolean>
        get() = EntityAccessor.accessNoGravityAccessor()

    public val POSE: EntityDataAccessor<Pose>
        get() = EntityAccessor.accessPoseAccessor()

    public val TICKS_FROZEN: EntityDataAccessor<Int>
        get() = EntityAccessor.accessTicksFrozenAccessor()

    public object Display {
        public val TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS: EntityDataAccessor<Int>
            get() = DisplayAccessor.accessTransformationInterpolationStartDeltaTicksAccessor()

        public val TRANSFORMATION_INTERPOLATION_DURATION: EntityDataAccessor<Int>
            get() = DisplayAccessor.accessTransformationInterpolationDurationAccessor()

        public val POS_ROT_INTERPOLATION_DURATION: EntityDataAccessor<Int>
            get() = DisplayAccessor.accessPosRotInterpolationDurationAccessor()

        public val TRANSLATION: EntityDataAccessor<Vector3fc>
            get() = DisplayAccessor.accessTranslationAccessor()

        public val SCALE: EntityDataAccessor<Vector3fc>
            get() = DisplayAccessor.accessScaleAccessor()

        public val LEFT_ROTATION: EntityDataAccessor<Quaternionfc>
            get() = DisplayAccessor.accessLeftRotationAccessor()

        public val RIGHT_ROTATION: EntityDataAccessor<Quaternionfc>
            get() = DisplayAccessor.accessRightRotationAccessor()

        public val BILLBOARD_RENDER_CONSTRAINTS: EntityDataAccessor<Byte>
            get() = DisplayAccessor.accessBillboardRenderConstraintsAccessor()

        public val BRIGHTNESS_OVERRIDE: EntityDataAccessor<Int>
            get() = DisplayAccessor.accessBrightnessOverrideAccessor()

        public val VIEW_RANGE: EntityDataAccessor<Float>
            get() = DisplayAccessor.accessViewRangeAccessor()

        public val SHADOW_RADIUS: EntityDataAccessor<Float>
            get() = DisplayAccessor.accessShadowRadiusAccessor()

        public val SHADOW_STRENGTH: EntityDataAccessor<Float>
            get() = DisplayAccessor.accessShadowStrengthAccessor()

        public val WIDTH: EntityDataAccessor<Float>
            get() = DisplayAccessor.accessWidthAccessor()

        public val HEIGHT: EntityDataAccessor<Float>
            get() = DisplayAccessor.accessHeightAccessor()

        public val GLOW_COLOR_OVERRIDE: EntityDataAccessor<Int>
            get() = DisplayAccessor.accessGlowColorOverrideAccessor()

        public object Item {
            public val ITEM_STACK: EntityDataAccessor<ItemStack>
                get() = ItemDisplayAccessor.accessItemStackAccessor()

            public val ITEM_DISPLAY_CONTEXT: EntityDataAccessor<Byte>
                get() = ItemDisplayAccessor.accessItemDisplayAccessor()
        }

        public object Block {
            public val BLOCK_STATE: EntityDataAccessor<BlockState>
                get() = BlockDisplayAccessor.accessBlockStateAccessor()
        }

        public object Text {
            public val TEXT: EntityDataAccessor<Component>
                get() = TextDisplayAccessor.accessTextAccessor()

            public val LINE_WIDTH: EntityDataAccessor<Int>
                get() = TextDisplayAccessor.accessLineWidthAccessor()

            public val BACKGROUND_COLOR: EntityDataAccessor<Int>
                get() = TextDisplayAccessor.accessBackgroundColorAccessor()

            public val TEXT_OPACITY: EntityDataAccessor<Byte>
                get() = TextDisplayAccessor.accessTextOpacityAccessor()

            public val STYLE_FLAGS: EntityDataAccessor<Byte>
                get() = TextDisplayAccessor.accessStyleFlagsAccessor()
        }
    }
}