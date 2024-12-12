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
