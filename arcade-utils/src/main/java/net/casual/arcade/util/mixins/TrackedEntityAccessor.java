/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(ChunkMap.TrackedEntity.class)
public interface TrackedEntityAccessor {
    @Accessor
    Entity getEntity();

    @Accessor
    Set<ServerPlayerConnection> getSeenBy();

    @Accessor
    ServerEntity getServerEntity();

    @Invoker("getEffectiveRange")
    int getRange();
}
