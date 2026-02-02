/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Interaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Interaction.class)
public interface InteractionAccessor {
    @Accessor("DATA_WIDTH_ID")
    static EntityDataAccessor<Float> accessWidthAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_HEIGHT_ID")
    static EntityDataAccessor<Float> accessHeightAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_RESPONSE_ID")
    static EntityDataAccessor<Boolean> accessResponseAccessor() {
        throw new AssertionError();
    }
}
