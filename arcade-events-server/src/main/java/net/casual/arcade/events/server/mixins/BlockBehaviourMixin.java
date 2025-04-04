/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.block.BlockDropEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @ModifyReturnValue(
        method = "getDrops",
        at = @At("RETURN")
    )
    private List<ItemStack> modifyBlockDrops(List<ItemStack> original, BlockState state, LootParams.Builder params) {
        BlockDropEvent event = new BlockDropEvent(state, params, original);
        GlobalEventHandler.Server.broadcast(event);
        return event.getDrops();
    }
}
