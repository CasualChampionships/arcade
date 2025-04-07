/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.mixins.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.pathfinder.PathTypeCache;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PathfindingContext.class)
public interface PathfindingContextAccessor {
    @Mutable
    @Accessor
    void setLevel(CollisionGetter level);

    @Mutable
    @Accessor
    void setCache(PathTypeCache cache);

    @Mutable
    @Accessor
    void setMobPosition(BlockPos mobPosition);

    @Mutable
    @Accessor
    void setMutablePos(BlockPos.MutableBlockPos pos);
}
