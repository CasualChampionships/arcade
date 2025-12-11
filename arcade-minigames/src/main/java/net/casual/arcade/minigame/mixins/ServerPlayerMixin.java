/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.managers.MinigameLevelManager;
import net.casual.arcade.minigame.managers.MinigamePlayerManager;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.casual.arcade.utils.math.location.LocationWithLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @ModifyExpressionValue(
		method = "isPvpAllowed",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;isPvpAllowed()Z"
		)
	)
	private boolean isPvpAllowed(boolean original) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		Minigame minigame = MinigameUtils.getMinigame(player);
		return original && (minigame == null || minigame.getSettings().canPvp.get(player));
	}

	@Inject(
		method = "isInvulnerableTo",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onIsInvulnerableTo(ServerLevel level, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		Minigame minigame = MinigameUtils.getMinigame(player);
		if (minigame != null && !minigame.getSettings().canTakeDamage.get(player)) {
			if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(
		method = "drop(Z)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onDropItem(boolean bl, CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		Minigame minigame = MinigameUtils.getMinigame(player);
		if (minigame != null && !minigame.getSettings().canDropItems.get(player)) {
			ci.cancel();
		}
	}

    @Inject(
        method = "findRespawnPositionAndUseSpawnBlock",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onFindRespawnPositionAndUseSpawnBlock(
        boolean useCharge,
        TeleportTransition.PostTeleportTransition post,
        CallbackInfoReturnable<TeleportTransition> cir
    ) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        TeleportTransition transition = this.getMinigameSpawn(player, false, post, true);
        if (transition != null) {
            cir.setReturnValue(transition);
        }
    }

    @WrapOperation(
        method = "findRespawnPositionAndUseSpawnBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/portal/TeleportTransition;missingRespawnBlock(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;"
        )
    )
    private TeleportTransition onFindMissingRespawnPosition(
        ServerPlayer player,
        TeleportTransition.PostTeleportTransition post,
        Operation<TeleportTransition> original
    ) {
        TeleportTransition transition = this.getMinigameSpawn(player, true, post, false);
        if (transition != null) {
            return transition;
        }

        return original.call(player, post);
    }

    @WrapOperation(
        method = "findRespawnPositionAndUseSpawnBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/portal/TeleportTransition;createDefault(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;"
        )
    )
    private TeleportTransition onFindDefaultRespawnPosition(
        ServerPlayer player,
        TeleportTransition.PostTeleportTransition post,
        Operation<TeleportTransition> original
    ) {
        TeleportTransition transition = this.getMinigameSpawn(player, false, post, false);
        if (transition != null) {
            return transition;
        }

        return original.call(player, post);
    }

	@WrapWithCondition(
		method = "restoreFrom",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ai/attributes/AttributeMap;assignBaseValues(Lnet/minecraft/world/entity/ai/attributes/AttributeMap;)V"
		)
	)
	private boolean onRestoreFrom(AttributeMap instance, AttributeMap map) {
        return MinigamePlayerManager.LOCAL_TRANSITION.get() == null;
    }

    @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
    @Definition(id = "KEEP_INVENTORY", field = "Lnet/minecraft/world/level/gamerules/GameRules;KEEP_INVENTORY:Lnet/minecraft/world/level/gamerules/GameRule;")
    @Expression("?.get(KEEP_INVENTORY)")
    @ModifyExpressionValue(
        method = "restoreFrom",
        at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private Object onIsKeepInventoryEnabled(Object original) {
		return MinigamePlayerManager.LOCAL_TRANSITION.get() == null && ((Boolean) original);
	}

    @Unique
    @Nullable
    private TeleportTransition getMinigameSpawn(
        ServerPlayer player,
        boolean missingRespawnBlock,
        TeleportTransition.PostTeleportTransition post,
        boolean requiresOverrideSpawnPoint
    ) {
        Minigame minigame = MinigameUtils.getMinigame(player);
        if (minigame == null) {
            return null;
        }
        MinigameLevelManager.SpawnLocation spawn = minigame.getLevels().getSpawn();
        if (requiresOverrideSpawnPoint && !spawn.getOverridesPlayerSpawnPoint()) {
            return null;
        }

        LocationWithLevel<ServerLevel> location = minigame.getLevels().getSpawn().get(player);
        if (location != null) {
            return LocationWithLevel.asTeleportTransition(
                location, Vec3.ZERO, missingRespawnBlock, false, Set.of(), post
            );
        }
        return null;
    }
}
