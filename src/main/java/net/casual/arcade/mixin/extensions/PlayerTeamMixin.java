package net.casual.arcade.mixin.extensions;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.team.TeamCreatedEvent;
import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ExtensionMap;
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
		GlobalEventHandler.broadcast(event);
	}

	@Unique
	@NotNull
	@Override
	public ExtensionMap getExtensionMap() {
		return this.arcade_extensionMap;
	}
}
