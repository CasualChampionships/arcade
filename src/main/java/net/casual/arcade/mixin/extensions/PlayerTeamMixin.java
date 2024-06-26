package net.casual.arcade.mixin.extensions;

import net.casual.arcade.ducks.Arcade$ExtensionHolder;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.team.TeamExtensionEvent;
import net.casual.arcade.extensions.ExtensionMap;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin implements Arcade$ExtensionHolder {
	@Unique
	private final ExtensionMap arcade$extensionMap = new ExtensionMap();

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onCreateTeam(Scoreboard scoreboard, String string, CallbackInfo ci) {
		TeamExtensionEvent event = new TeamExtensionEvent((PlayerTeam) (Object) this);
		GlobalEventHandler.broadcast(event);
	}

	@Unique
	@Override
	public ExtensionMap arcade$getExtensionMap() {
		return this.arcade$extensionMap;
	}
}
