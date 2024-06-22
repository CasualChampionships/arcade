package net.casual.arcade.mixin.events;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.entity.EntityMoveEvent;
import net.casual.arcade.events.player.PlayerMoveEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow public abstract Vec3 position();

	@Inject(
		method = "setPosRaw",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/entity/EntityInLevelCallback;onMove()V",
			shift = At.Shift.AFTER
		)
	)
	private void onEntityMove(double x, double y, double z, CallbackInfo ci) {
		EntityMoveEvent entityMoveEvent = new EntityMoveEvent((Entity) (Object) this, this.position());
		GlobalEventHandler.broadcast(entityMoveEvent);
		if ((Object) this instanceof ServerPlayer player) {
			PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(player, this.position());
			GlobalEventHandler.broadcast(playerMoveEvent);
		}
	}
}
