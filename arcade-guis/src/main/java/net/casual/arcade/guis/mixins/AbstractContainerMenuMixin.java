/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.guis.inventory.VirtualInventory;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.NonInteractiveResultSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
    @WrapOperation(
        method = {"addInventoryHotbarSlots", "addInventoryExtendedSlots"},
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/world/Container;III)Lnet/minecraft/world/inventory/Slot;"
        )
    )
    private Slot onCreateInventorySlot(Container container, int slot, int x, int y, Operation<Slot> original) {
        if (container instanceof VirtualInventory) {
            return new NonInteractiveResultSlot(container, slot, x, y);
        }
        return original.call(container, slot, x, y);
    }

    @Inject(
        method = "doClick",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onDoClick(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        Inventory inventory = player.getInventory();
        if (inventory instanceof VirtualInventory && clickType == ClickType.SWAP) {
            ci.cancel();
        }
    }
}
