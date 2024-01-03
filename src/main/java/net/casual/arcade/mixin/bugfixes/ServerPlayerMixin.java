package net.casual.arcade.mixin.bugfixes;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
		super(level, pos, yRot, gameProfile);
	}

	@Inject(
		method = "restoreFrom",
		at = @At("HEAD")
	)
	private void onRestorePlayer(ServerPlayer that, boolean keepEverything, CallbackInfo ci) {
		if (keepEverything) {
			this.getAttributes().assignValues(that.getAttributes());
		}
	}
}
