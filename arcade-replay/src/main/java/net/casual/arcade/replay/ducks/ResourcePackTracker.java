/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.ducks;

import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

@ApiStatus.Internal
public interface ResourcePackTracker {
    void arcade_addPacks(Collection<ClientboundResourcePackPushPacket> packs);

    Collection<ClientboundResourcePackPushPacket> arcade_getPacks();
}
