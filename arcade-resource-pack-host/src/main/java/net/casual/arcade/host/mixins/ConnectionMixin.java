/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host.mixins;

import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.casual.arcade.host.ducks.MutableConnectionAddressHolder;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Connection.class)
public class ConnectionMixin implements MutableConnectionAddressHolder {
    @Unique private ObjectIntPair<String> arcade$address = null;

    @Override
    public void arcade$setConnectionAddress(String ip, int port) {
        this.arcade$address = new ObjectIntImmutablePair<>(ip, port);
    }

    @Nullable
    @Override
    public ObjectIntPair<String> arcade$getConnectionAddress() {
        return this.arcade$address;
    }
}
