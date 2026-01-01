/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent.Companion.replacePacketRecursively
import net.casual.arcade.events.server.player.PlayerSlotClickEvent
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.disableGlint
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.minecraft.core.Holder
import net.minecraft.core.RegistryAccess
import net.minecraft.core.component.DataComponents
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.item.equipment.Equippable
import kotlin.jvm.optionals.getOrNull

internal class PlayerCameraOverlayExtension(player: ServerPlayer): PlayerExtension(player) {
    private var overlay: ResourceLocation? = null

    fun setOverlay(overlay: ResourceLocation) {
        this.overlay = overlay
        this.updateHeadSlot()
    }

    fun clearOverlay() {
        this.overlay = null
        this.updateHeadSlot()
    }

    fun overlaid(stack: ItemStack, access: RegistryAccess): ItemStack {
        val overlay = this.overlay ?: return stack
        val overlaid = if (stack.isEmpty) this.createDummyStack(access) else stack.copy()
        val equippable = Equippable.builder(EquipmentSlot.HEAD)
            .setEquipSound(Holder.direct(SoundEvents.EMPTY))
            .setCameraOverlay(overlay)
        val existing = overlaid.get(DataComponents.EQUIPPABLE)
        if (existing != null) {
            existing.assetId().ifPresent(equippable::setAsset)
            equippable.setSwappable(existing.swappable)
        }
        overlaid.set(DataComponents.EQUIPPABLE, equippable.build())
        return overlaid
    }

    private fun createDummyStack(access: RegistryAccess): ItemStack {
        val stack = ItemUtils.modelled(Items.AIR).hideTooltip()
        val binding = access.get(Enchantments.BINDING_CURSE).getOrNull()
        if (binding != null) {
            stack.enchant(binding, 1)
            stack.disableGlint()
        }
        return stack
    }
    
    private fun updateHeadSlot() {
        val menu = this.player.inventoryMenu
        val item = menu.getSlot(HEAD_SLOT).item
        val update = ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), HEAD_SLOT, item)
        this.player.connection.send(update)
    }

    companion object {
        private const val HEAD_SLOT = 5

        @JvmStatic
        val ServerPlayer.cameraOverlayExtension: PlayerCameraOverlayExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerExtensionEvent> {
                it.addExtension(::PlayerCameraOverlayExtension)
            }
            GlobalEventHandler.Server.register<PlayerSlotClickEvent>(phase = PlayerSlotClickEvent.PHASE_POST, listener = ::onPlayerSlotClick)
            GlobalEventHandler.Server.register<PlayerClientboundPacketEvent>(::onPlayerClientboundPacket)
        }

        private fun onPlayerSlotClick(event: PlayerSlotClickEvent) {
            if (event.menu is InventoryMenu && event.index == HEAD_SLOT) {
                val extension = event.player.cameraOverlayExtension
                if (extension.overlay != null) {
                    extension.updateHeadSlot()
                }
            }
        }

        private fun onPlayerClientboundPacket(event: PlayerClientboundPacketEvent) {
            event.replacePacketRecursively(::modifyPacket)
        }

        private fun modifyPacket(player: ServerPlayer, packet: Packet<*>): Packet<*> {
            if (packet is ClientboundContainerSetSlotPacket) {
                if (packet.containerId == 0 && packet.slot == HEAD_SLOT) {
                    val replacement = player.cameraOverlayExtension.overlaid(packet.item, player.registryAccess())
                    if (replacement !== packet.item) {
                        return ClientboundContainerSetSlotPacket(0, packet.stateId, HEAD_SLOT, replacement)
                    }
                }
            } else if (packet is ClientboundContainerSetContentPacket) {
                if (packet.containerId == 0) {
                    val original = packet.items[HEAD_SLOT]
                    val replacement = player.cameraOverlayExtension.overlaid(original, player.registryAccess())
                    if (replacement !== original) {
                        val copy = ArrayList(packet.items)
                        copy[HEAD_SLOT] = replacement
                        return ClientboundContainerSetContentPacket(0, packet.stateId, copy, packet.carriedItem)
                    }
                }
            }
            return packet
        }
    }
}