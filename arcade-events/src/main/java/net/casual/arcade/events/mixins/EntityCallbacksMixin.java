package net.casual.arcade.events.mixins;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.entity.EntityStartTrackingEvent;
import net.casual.arcade.events.entity.EntityStopTrackingEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public class EntityCallbacksMixin {
	@Shadow @Final ServerLevel field_26936;

	@Inject(
		method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V",
		at = @At("HEAD")
	)
	private void onTrackEntity(Entity entity, CallbackInfo ci) {
		EntityStartTrackingEvent event = new EntityStartTrackingEvent(entity, this.field_26936);
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V",
		at = @At("HEAD")
	)
	private void onStopTrackingEntity(Entity entity, CallbackInfo ci) {
		EntityStopTrackingEvent event = new EntityStopTrackingEvent(entity, this.field_26936);
		GlobalEventHandler.broadcast(event);
	}
}
