package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.player.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(
		method = "use",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onUse(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResult> cir) {
		if (player instanceof ServerPlayer serverPlayer) {
			PlayerItemUseEvent event = new PlayerItemUseEvent(serverPlayer, (ItemStack) (Object) this, usedHand);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
				cir.setReturnValue(event.result());
			}
		}
	}

	@Inject(
		method = "useOn",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (context.getPlayer() instanceof ServerPlayer player) {
			PlayerItemUseOnEvent event = new PlayerItemUseOnEvent(player, (ItemStack) (Object) this, context);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
				cir.setReturnValue(event.result());
			}
		}
	}

	@Inject(
		method = "finishUsingItem",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onFinishUsing(Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
		if (livingEntity instanceof ServerPlayer player) {
			PlayerItemFinishEvent event = new PlayerItemFinishEvent(player, (ItemStack) (Object) this);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
				cir.setReturnValue(event.result());
			}
		}
	}

	@WrapOperation(
		method = "releaseUsing",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/Item;releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Z"
		)
	)
	private boolean onReleaseUsing(
		Item instance,
		ItemStack stack,
		Level level,
		LivingEntity livingEntity,
		int timeCharged,
		Operation<Boolean> original
	) {
		if (livingEntity instanceof ServerPlayer player) {
			PlayerItemReleaseEvent event = new PlayerItemReleaseEvent(player, stack, timeCharged);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
				return false;
			}
		}
		return original.call(instance, stack, level, livingEntity, timeCharged);
	}

	@Inject(
		method = "onCraftedBy",
		at = @At("HEAD")
	)
	private void onCraft(Level level, Player player, int amount, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			PlayerCraftEvent event = new PlayerCraftEvent(serverPlayer, (ItemStack) (Object) this);
			GlobalEventHandler.Server.broadcast(event);
		}
	}
}
