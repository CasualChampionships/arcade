/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.npc.FakePlayer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.Set;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
    @Shadow @Final private Entity entity;

    @ModifyExpressionValue(
        method = "sendPairingData",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/attributes/AttributeMap;getSyncableAttributes()Ljava/util/Collection;"
        )
    )
    private Collection<AttributeInstance> onGetSyncAttributes(Collection<AttributeInstance> original) {
        if (this.entity instanceof FakePlayer) {
            AttributeSupplier supplier = DefaultAttributes.getSupplier(EntityType.PLAYER);
            original.removeIf(instance -> !supplier.hasAttribute(instance.getAttribute()));
        }
        return original;
    }

    @ModifyExpressionValue(
        method = "sendDirtyEntityData",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/attributes/AttributeMap;getAttributesToSync()Ljava/util/Set;"
        )
    )
    private Set<AttributeInstance> onGetSyncAttributes(Set<AttributeInstance> original) {
        if (this.entity instanceof FakePlayer) {
            AttributeSupplier supplier = DefaultAttributes.getSupplier(EntityType.PLAYER);
            original.removeIf(instance -> !supplier.hasAttribute(instance.getAttribute()));
        }
        return original;
    }
}
