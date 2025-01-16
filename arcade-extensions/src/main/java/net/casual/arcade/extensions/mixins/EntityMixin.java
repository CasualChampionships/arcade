/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ExtensionMap;
import net.casual.arcade.extensions.event.EntityExtensionEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements ExtensionHolder {
    @Unique private ExtensionMap arcade$extensions;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void onCreateEntity(
        EntityType<?> entityType,
        Level level,
        CallbackInfo ci
    ) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof ServerPlayer) {
            return;
        }
        this.arcade$extensions = new ExtensionMap();
        EntityExtensionEvent event = new EntityExtensionEvent(entity);
        GlobalEventHandler.Server.broadcast(event);
    }

    @Inject(
        method = "load",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"
        )
    )
    private void onLoadPlayer(CompoundTag compound, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer) {
            return;
        }
        CompoundTag tag = compound.getCompound("arcade");
        ExtensionHolder.deserialize(this, tag);
    }

    @Inject(
        method = "saveWithoutId",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"
        )
    )
    private void onSavePlayer(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag arcade = new CompoundTag();
        ExtensionHolder.serialize(this, arcade);
        compound.put("arcade", arcade);
    }

    @NotNull
    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public ExtensionMap getExtensionMap() {
        return this.arcade$extensions;
    }
}
