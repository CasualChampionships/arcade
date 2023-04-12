package net.casualuhc.arcade.mixin.extensions;

import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.team.TeamCreatedEvent;
import net.casualuhc.arcade.extensions.ExtensionHolder;
import net.casualuhc.arcade.extensions.ExtensionMap;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin implements ExtensionHolder {
	@Unique
	private final ExtensionMap arcade_extensionMap = new ExtensionMap();

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onCreateTeam(Scoreboard scoreboard, String string, CallbackInfo ci) {
		TeamCreatedEvent event = new TeamCreatedEvent((PlayerTeam) (Object) this);
		EventHandler.broadcast(event);
	}

	@Unique
	@NotNull
	@Override
	public ExtensionMap getExtensionMap() {
		return this.arcade_extensionMap;
	}
}
