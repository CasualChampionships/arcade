/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(ServerboundTeleportToEntityPacket.class)
public interface ServerboundTeleportToEntityPacketAccessor {
	@Accessor("uuid")
	UUID getUUID();
}
