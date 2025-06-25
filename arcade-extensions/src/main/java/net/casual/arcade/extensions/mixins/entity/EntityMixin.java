/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins.entity;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.extensions.Extension;
import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ExtensionMap;
import net.casual.arcade.extensions.TransferableEntityExtension;
import net.casual.arcade.extensions.TransferableEntityExtension.TransferReason;
import net.casual.arcade.extensions.event.EntityExtensionEvent;
import net.casual.arcade.utils.ArcadeUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
            target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V"
        )
    )
    private void onLoad(ValueInput input, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer) {
            return;
        }
        ValueInput child = input.childOrEmpty(ArcadeUtils.MOD_ID);
        ExtensionHolder.deserialize(this, child);
    }

    @Inject(
        method = "saveWithoutId",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V"
        )
    )
    private void onSave(ValueOutput output, CallbackInfo ci) {
        ValueOutput child = output.child(ArcadeUtils.MOD_ID);
        ExtensionHolder.serialize(this, child);
    }

    @Inject(
        method = "restoreFrom",
        at = @At("HEAD")
    )
    private void onRestoreEntity(Entity entity, CallbackInfo ci) {
        for (Extension extension : ExtensionHolder.all((ExtensionHolder) entity)) {
            if (extension instanceof TransferableEntityExtension transferable) {
                TransferReason reason = TransferReason.Other;
                Extension transferred = transferable.transfer((Entity) (Object) this, reason);
                this.arcade$extensions.add(transferred);
            }
        }
    }

    @NotNull
    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public ExtensionMap getExtensionMap() {
        return this.arcade$extensions;
    }
}
