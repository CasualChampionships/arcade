/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins;

import net.casual.arcade.util.ducks.ConnectionFaultHolder;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin implements ConnectionFaultHolder {
    @Unique private boolean arcade_hasTimedOut = false;
    @Unique private Throwable arcade_packetError = null;

    @Inject(
        method = {"handleKeepAlive", "keepConnectionAlive"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerCommonPacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"
        )
    )
    private void onConnectionTimedOut(CallbackInfo ci) {
        this.arcade_hasTimedOut = true;
    }

    @Override
    public void arcade_setTimeOut(boolean timedOut) {
        this.arcade_hasTimedOut = timedOut;
    }

    @Override
    public boolean arcade_hasTimeOut() {
        return this.arcade_hasTimedOut;
    }

    @Override
    public void arcade_setPacketError(Throwable packetError) {
        this.arcade_packetError = packetError;
    }

    @Override
    @Nullable
    public Throwable arcade_getPacketError() {
        return this.arcade_packetError;
    }
}
