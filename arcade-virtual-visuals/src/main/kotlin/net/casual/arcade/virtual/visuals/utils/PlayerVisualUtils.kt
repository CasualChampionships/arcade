/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.utils

import net.casual.arcade.virtual.visuals.extensions.PlayerCameraOverlayExtension.Companion.cameraOverlayExtension
import net.minecraft.core.component.DataComponents
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityEvent
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.DeathProtection

public fun ServerPlayer.setCameraOverlay(overlay: Identifier) {
    this.cameraOverlayExtension.setOverlay(overlay)
}

public fun ServerPlayer.clearCameraOverlay() {
    this.cameraOverlayExtension.clearOverlay()
}

public fun ServerPlayer.displayTotemEffect(display: ItemStack) {
    val copy = display.copy()
    copy.set(DataComponents.DEATH_PROTECTION, DeathProtection(listOf()))
    val slot = InventoryMenu.SHIELD_SLOT
    val menu = this.inventoryMenu
    val item = menu.getSlot(slot).item
    val packets = listOf(
        ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), slot, copy),
        ClientboundEntityEventPacket(this, EntityEvent.PROTECTED_FROM_DEATH),
        ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), slot, item)
    )
    this.connection.send(ClientboundBundlePacket(packets))
}