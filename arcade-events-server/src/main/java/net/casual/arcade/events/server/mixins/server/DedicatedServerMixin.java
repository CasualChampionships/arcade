/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.events.server.ServerSaveEvent;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {
    @WrapOperation(
        method = "initServer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/dedicated/DedicatedServer;saveEverything(ZZZ)Z"
        )
    )
    private boolean markSaveAsInitial(
        DedicatedServer server,
        boolean silent,
        boolean flush,
        boolean force,
        Operation<Boolean> original
    ) {
        return ScopedValue.where(ServerSaveEvent.SAVE_REASON, ServerSaveEvent.Reason.Initial).call(() -> {
            return original.call(server, silent, flush, force);
        });
    }
}
