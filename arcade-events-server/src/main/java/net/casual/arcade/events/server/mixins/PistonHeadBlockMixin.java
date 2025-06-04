/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PistonHeadBlock.class)
public class PistonHeadBlockMixin {
    @ModifyExpressionValue(
        method = "playerWillDestroy",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;preventsBlockDrops()Z"
        )
    )
    private boolean shouldPreventBlockDrops(boolean original) {
        // Only creative players handled here for some reason.
        // We make it so all players are handled here so that
        // we have the context of which player broke the block.
        return true;
    }

    @Redirect(
        method = "playerWillDestroy",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"
        )
    )
    private boolean onPlayerDestroyBlock(
        Level instance,
        BlockPos pos,
        boolean drop,
        @Local(argsOnly = true) Player player
    ) {
        return instance.destroyBlock(pos, !player.preventsBlockDrops(), player);
    }
}
