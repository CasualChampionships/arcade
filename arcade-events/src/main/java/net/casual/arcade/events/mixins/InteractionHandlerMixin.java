package net.casual.arcade.events.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerEntityPositionInteractionEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public class InteractionHandlerMixin {
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
}
