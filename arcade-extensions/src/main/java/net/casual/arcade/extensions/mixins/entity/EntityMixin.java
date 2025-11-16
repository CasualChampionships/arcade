/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins.entity;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.extensions.*;
import net.casual.arcade.extensions.event.EntityExtensionEvent;
import net.casual.arcade.utils.ArcadeUtils;
import net.casual.arcade.utils.entity.EntityTransferReason;
import net.casual.arcade.utils.impl.DelayedInvokers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements ExtensionHolder {
    @Shadow private Level level;

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
        if (level.isClientSide()) {
            return;
        }

        Entity entity = (Entity) (Object) this;
        if (entity instanceof Player || !EntityExtension.SHOULD_ATTACH_EXTENSION.get()) {
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
        if ((Object) this instanceof ServerPlayer || this.level.isClientSide()) {
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
        if (!this.level.isClientSide()) {
            ValueOutput child = output.child(ArcadeUtils.MOD_ID);
            ExtensionHolder.serialize(this, child);
        }
    }

    @Inject(
        method = "restoreFrom",
        at = @At("HEAD")
    )
    private void onRestoreEntity(
        Entity entity,
        CallbackInfo ci,
        @Share("delayed") LocalRef<DelayedInvokers.Simple> delayedRef
    ) {
        if (entity instanceof ServerPlayer || this.level.isClientSide()) {
            return;
        }

        DelayedInvokers.Simple delayed = new DelayedInvokers.Simple();
        delayedRef.set(delayed);

        for (Extension extension : ExtensionHolder.all((ExtensionHolder) entity)) {
            if (extension instanceof TransferableEntityExtension transferable) {
                EntityTransferReason reason = EntityTransferReason.Other;
                Extension transferred = transferable.transfer((Entity) (Object) this, reason, delayed);
                this.arcade$extensions.add(transferred);
            }
        }
    }

    @Inject(
        method = "restoreFrom",
        at = @At("RETURN")
    )
    private void onRestoreEntityPost(
        Entity entity,
        CallbackInfo ci,
        @Share("delayed") LocalRef<DelayedInvokers.Simple> delayedRef
    ) {
        DelayedInvokers.Simple delayed = delayedRef.get();
        if (delayed != null) {
            delayed.invoke();
        }
    }

    @NotNull
    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public ExtensionMap getExtensionMap() {
        return this.arcade$extensions;
    }
}
