package net.casual.arcade.util.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.util.ducks.CustomMOTD;
import net.casual.arcade.utils.ServerUtils;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements CustomMOTD {
	@Shadow @Nullable private ServerStatus status;
	@Shadow private long lastServerStatus;

	@Shadow protected abstract ServerStatus buildServerStatus();

	@Unique
	private Component arcade$motd = null;

	@Inject(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/thread/ReentrantBlockableEventLoop;<init>(Ljava/lang/String;)V",
			shift = At.Shift.AFTER
		)
	)
	private void onCreateServerInstance(CallbackInfo ci) {
		ServerUtils.setServer((MinecraftServer) (Object) this);
	}

	@ModifyExpressionValue(
		method = "buildServerStatus",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/chat/Component;nullToEmpty(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;"
		)
	)
	private Component getMOTD(Component original) {
		if (this.arcade$motd != null) {
			return this.arcade$motd;
		}
		return original;
	}

	@Override
	public void arcade$setMOTD(Component message) {
		this.arcade$motd = message;
		this.status = this.buildServerStatus();
		this.lastServerStatus = Util.getNanos();
	}

	@Override
	public Component arcade$getMOTD() {
		return this.arcade$motd;
	}
}
