package net.casual.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.entity.player.ExtendedGameMode;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerEntityPositionInteractionEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public class InteractionHandlerMixin {
	@Shadow @Final ServerGamePacketListenerImpl field_28963;

	@WrapOperation(
		method = "method_33898",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;interactAt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
		)
	)
	private static InteractionResult shouldInteractAt(
		Entity instance,
		Player player,
		Vec3 vec,
		InteractionHand hand,
		Operation<InteractionResult> original
	) {
		if (player instanceof ServerPlayer serverPlayer) {
			PlayerEntityPositionInteractionEvent event = new PlayerEntityPositionInteractionEvent(serverPlayer, instance, hand, vec);
			GlobalEventHandler.broadcast(event);
			if (event.isCancelled()) {
				return event.result();
			}
		}
		return original.call(instance, player, vec, hand);
	}

	@Inject(
		method = "onAttack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"
		),
		cancellable = true
	)
	private void onAttackInvalid(CallbackInfo ci) {
		if (ExtendedGameMode.getExtendedGameMode(this.field_28963.player) == ExtendedGameMode.AdventureSpectator) {
			// Vanilla client is silly and will sometimes do this
			ci.cancel();
		}
	}
}
