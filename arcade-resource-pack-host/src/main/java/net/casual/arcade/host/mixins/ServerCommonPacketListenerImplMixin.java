/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host.mixins;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.casual.arcade.host.ducks.ConnectionAddressHolder;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin implements ConnectionAddressHolder {
    @Shadow @Final protected Connection connection;

    @Nullable
    @Override
    public ObjectIntPair<String> arcade$getConnectionAddress() {
        return ((ConnectionAddressHolder) this.connection).arcade$getConnectionAddress();
    }
}
