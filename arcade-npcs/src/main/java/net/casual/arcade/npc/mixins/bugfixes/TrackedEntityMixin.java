/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.mixins.bugfixes;

import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public class TrackedEntityMixin {
    @Shadow
    @Final
    private Set<ServerPlayerConnection> seenBy;

    @Inject(
        method = "broadcastRemoved",
        at = @At("TAIL")
    )
    private void clearSeenBy(CallbackInfo ci) {
        // Vanilla doesn't clear this list and thus may result in
        // other mods trying to access the players tracking this entity
        // when the entity has been removed (both on the server & their client)
        this.seenBy.clear();
    }
}
