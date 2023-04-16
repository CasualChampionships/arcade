package net.casualuhc.arcade.mixin.scoreboard;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundSetPlayerTeamPacket.Parameters.class)
public interface ParametersAccessor {
	@Mutable
	@Accessor("playerPrefix")
	void setPrefix(Component prefix);
}
