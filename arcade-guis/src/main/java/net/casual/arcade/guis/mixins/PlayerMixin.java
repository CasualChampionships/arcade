/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.guis.ducks.ModifiableInventory;
import net.casual.arcade.guis.inventory.CustomInventory;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin implements ModifiableInventory {
    @Unique private InventoryMenu arcade$vanillaInventoryMenu;
    @Unique private Inventory arcade$vanillaInventory;

    @Shadow @Final @Mutable public InventoryMenu inventoryMenu;
    @Shadow @Final @Mutable private Inventory inventory;

    @Shadow public AbstractContainerMenu containerMenu;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void onInit(CallbackInfo ci) {
        this.arcade$vanillaInventory = this.inventory;
        this.arcade$vanillaInventoryMenu = this.inventoryMenu;
    }

    @ModifyExpressionValue(
        method = "addAdditionalSaveData",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/player/Player;inventory:Lnet/minecraft/world/entity/player/Inventory;",
            opcode = Opcodes.GETFIELD
        )
    )
    private Inventory getInventory(Inventory original) {
        return this.arcade$vanillaInventory;
    }

    @Override
    public void arcade$setCustomInventory(CustomInventory inventory) {
        if ((Object) this instanceof ServerPlayer player) {
            InventoryMenu previous = this.inventoryMenu;
            this.inventory = inventory;
            this.inventoryMenu = inventory.menu();
            if (this.containerMenu == previous) {
                this.containerMenu = this.inventoryMenu;
            }
            player.initInventoryMenu();
            player.connection.send(new ClientboundSetHeldSlotPacket(this.inventory.getSelectedSlot()));
        }
    }

    @Override
    public void arcade$removeCustomInventory() {
        InventoryMenu previous = this.inventoryMenu;
        this.inventory = this.arcade$vanillaInventory;
        this.inventoryMenu = this.arcade$vanillaInventoryMenu;
        if (this.containerMenu == previous) {
            this.containerMenu = this.inventoryMenu;
        }
    }
}
