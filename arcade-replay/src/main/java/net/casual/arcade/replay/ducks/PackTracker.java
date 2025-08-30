/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.ducks;

import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

@ApiStatus.Internal
public interface PackTracker {
    void replay$addPacks(Collection<ClientboundResourcePackPushPacket> packs);

    Collection<ClientboundResourcePackPushPacket> replay$getPacks();
}
