/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.BlockDisplay.class)
public interface BlockDisplayAccessor {
    @Accessor("DATA_BLOCK_STATE_ID")
    static EntityDataAccessor<BlockState> accessBlockStateAccessor() {
        throw new AssertionError();
    }
}
