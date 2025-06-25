/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.managers.MinigamePlayerManager;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.casual.arcade.utils.ArcadeUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @WrapOperation(
        method = "respawn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;"
        )
    )
    private TeleportTransition onFindRespawnLocation(
        ServerPlayer instance,
        boolean bl,
        TeleportTransition.PostTeleportTransition postTeleportTransition,
        Operation<TeleportTransition> original,
        // If this is changed, it must also be updated in extensions#PlayerListMixin
        @Share(namespace = ArcadeUtils.MOD_ID, value = "isMinigameRespawn") LocalBooleanRef isMinigameRespawn
    ) {
        if (MinigamePlayerManager.LOCAL_TRANSITION.get() != null) {
            isMinigameRespawn.set(true);
            return MinigamePlayerManager.LOCAL_TRANSITION.get();
        }
        return original.call(instance, bl, postTeleportTransition);
    }

    @WrapWithCondition(
        method = "respawn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;copyRespawnPosition(Lnet/minecraft/server/level/ServerPlayer;)V"
        )
    )
    private boolean onCopyRespawnPosition(
        ServerPlayer instance,
        ServerPlayer player,
        @Share(namespace = ArcadeUtils.MOD_ID, value = "isMinigameRespawn") LocalBooleanRef isMinigameRespawn
    ) {
        return !isMinigameRespawn.get();
    }

    @ModifyExpressionValue(
        method = "respawn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;getTags()Ljava/util/Set;"
        )
    )
    private Set<String> onGetTags(
        Set<String> original,
        @Share(namespace = ArcadeUtils.MOD_ID, value = "isMinigameRespawn") LocalBooleanRef isMinigameRespawn
    ) {
        if (isMinigameRespawn.get()) {
            return Set.of();
        }
        return original;
    }

    @WrapWithCondition(
        method = "save",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/PlayerDataStorage;save(Lnet/minecraft/world/entity/player/Player;)V"
        )
    )
    private boolean onSavePlayerData(PlayerDataStorage instance, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Minigame minigame = MinigameUtils.getMinigame(serverPlayer);
            return minigame == null || minigame.getPlayers().getKeepPlayerData();
        }
        return true;
    }
}
