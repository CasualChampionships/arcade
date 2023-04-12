package net.casualuhc.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.player.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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
	private void onUse(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
		if (player instanceof ServerPlayer serverPlayer) {
			PlayerItemUseEvent event = new PlayerItemUseEvent(serverPlayer, (ItemStack) (Object) this, level, usedHand);
			EventHandler.broadcast(event);
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
			EventHandler.broadcast(event);
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
			PlayerItemFinishEvent event = new PlayerItemFinishEvent(player, (ItemStack) (Object) this, level);
			EventHandler.broadcast(event);
			if (event.isCancelled()) {
				cir.setReturnValue(event.result());
			}
		}
	}

	@WrapWithCondition(
		method = "releaseUsing",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/Item;releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"
		)
	)
	private boolean onReleaseUsing(Item instance, ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
		if (livingEntity instanceof ServerPlayer player) {
			PlayerItemReleaseEvent event = new PlayerItemReleaseEvent(player, stack, level, timeCharged);
			EventHandler.broadcast(event);
			return !event.isCancelled();
		}
		return true;
	}

	@Inject(
		method = "onCraftedBy",
		at = @At("HEAD")
	)
	private void onCraft(Level level, Player player, int amount, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			PlayerCraftEvent event = new PlayerCraftEvent(serverPlayer, (ItemStack) (Object) this);
			EventHandler.broadcast(event);
		}
	}
}
