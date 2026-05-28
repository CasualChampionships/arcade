/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.flashback;

import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ClientboundLevelChunkPacketData.class)
public interface ClientboundLevelChunkPacketDataAccessor {
    @Accessor("buffer")
    byte[] arcade_getBuffer();

    @Accessor("blockEntitiesData")
    List<ClientboundLevelChunkPacketData.BlockEntityInfo> arcade_getBlockEntitiesData();
}
