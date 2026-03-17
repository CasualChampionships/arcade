/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.TextDisplay.class)
public interface TextDisplayAccessor {
    @Accessor("DATA_TEXT_ID")
    static EntityDataAccessor<Component> accessTextAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_LINE_WIDTH_ID")
    static EntityDataAccessor<Integer> accessLineWidthAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_BACKGROUND_COLOR_ID")
    static EntityDataAccessor<Integer> accessBackgroundColorAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_TEXT_OPACITY_ID")
    static EntityDataAccessor<Byte> accessTextOpacityAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_STYLE_FLAGS_ID")
    static EntityDataAccessor<Byte> accessStyleFlagsAccessor() {
        throw new AssertionError();
    }
}
