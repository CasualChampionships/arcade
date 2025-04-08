/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
    @ModifyExpressionValue(
        method = "sendPairingData",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/server/level/ServerEntity;trackedDataValues:Ljava/util/List;"
        )
    )
    private List<SynchedEntityData.DataValue<?>> onGetTrackedDataValues(
        @Nullable List<SynchedEntityData.DataValue<?>> original
    ) {
        // Sending an empty packet is suboptimal but fixes compatability
        // with mods that modify entity data on a packet level
        return original == null ? List.of() : original;
    }
}
