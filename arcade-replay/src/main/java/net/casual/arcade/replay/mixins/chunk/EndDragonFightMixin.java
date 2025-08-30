/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.chunk;

import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecordable;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorders;
import net.casual.arcade.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndDragonFight.class)
public class EndDragonFightMixin {
    @Shadow
    @Final
    private BlockPos origin;

    @Shadow
    @Final
    private ServerBossEvent dragonEvent;

    @Shadow
    @Final
    private ServerLevel level;

    @Inject(
        method = "updatePlayers",
        at = @At("TAIL")
    )
    private void onUpdate(CallbackInfo ci) {
        BoundingBox box = MathUtils.createBoundingBox(this.origin, 192);
        ReplayChunkRecorders.updateRecordable((ReplayChunkRecordable) this.dragonEvent, this.level.dimension(), box);
    }
}
