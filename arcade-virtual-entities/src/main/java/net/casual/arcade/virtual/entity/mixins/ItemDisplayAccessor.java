/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.ItemDisplay.class)
public interface ItemDisplayAccessor {
    @Accessor("DATA_ITEM_STACK_ID")
    static EntityDataAccessor<ItemStack> accessItemStackAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_ITEM_DISPLAY_ID")
    static EntityDataAccessor<Byte> accessItemDisplayAccessor() {
        throw new AssertionError();
    }
}
