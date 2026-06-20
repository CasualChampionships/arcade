/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.util.ducks.CustomMOTD;
import net.casual.arcade.utils.component.event.CustomClickEventRegistry;
import net.casual.arcade.utils.coroutine.ServerCoroutineUtils;
import net.casual.arcade.utils.server.ServerSingleton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(value = MinecraftServer.class, priority = 1100)
public abstract class MinecraftServerMixin implements CustomMOTD {
	@Unique private Component arcade_motd = null;

	@Shadow @Nullable private ServerStatus status;
	@Shadow private long lastServerStatus;

	@Shadow protected abstract ServerStatus buildServerStatus();

	@Inject(
		method = "<init>",
		at = @At("CTOR_HEAD")
	)
	private void onCreateServerInstance(CallbackInfo ci) {
		ServerSingleton.setServer((MinecraftServer) (Object) this);
	}

	@ModifyExpressionValue(
		method = "buildServerStatus",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/chat/Component;nullToEmpty(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;"
		)
	)
	private Component getMOTD(Component original) {
		if (this.arcade_motd != null) {
			return this.arcade_motd;
		}
		return original;
	}

	@Definition(id = "push", method = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V")
	@Expression("?.push('tallying')")
	@Inject(
		method = "tickServer",
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private void onServerTick(BooleanSupplier haveTime, CallbackInfo ci) {
		ProfilerFiller filler = Profiler.get();
		filler.push("coroutine_tick_delayed_tasks");
		ServerCoroutineUtils.INSTANCE.tickServer((MinecraftServer) (Object) this);
		filler.pop();

		CustomClickEventRegistry.onServerTick();
	}

    @Inject(
        method = "stopServer",
        at = @At("RETURN")
    )
    private void onStopServer(CallbackInfo ci) {
        ServerSingleton.setServer(null);
        ServerCoroutineUtils.INSTANCE.stopServer((MinecraftServer) (Object) this);
    }

	@Override
	public void arcade_setMOTD(Component message) {
		this.arcade_motd = message;
		this.status = this.buildServerStatus();
		this.lastServerStatus = Util.getNanos();
	}

	@Override
	@Nullable
	public Component arcade_getMOTD() {
		return this.arcade_motd;
	}
}
