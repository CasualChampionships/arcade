/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.inventory

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.player.*
import net.casual.arcade.events.utils.register
import net.casual.arcade.guis.utils.SlotInteractAction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Inventory

internal object CustomInventoryEvents {
    fun registerEvents() {
        GlobalEventHandler.Server.register<PlayerClientSwingHandEvent>(priority = Int.MIN_VALUE, listener = ::onPlayerClientSwingHand)
        GlobalEventHandler.Server.register<PlayerTryAttackEvent>(priority = Int.MIN_VALUE, listener = ::onPlayerTryAttack)
        GlobalEventHandler.Server.register<PlayerItemUseEvent>(priority = Int.MIN_VALUE, phase = PlayerItemUseEvent.PHASE_PRE, listener = ::onPlayerItemUse)
        GlobalEventHandler.Server.register<PlayerEntityInteractionEvent>(priority = Int.MIN_VALUE, listener = ::onPlayerEntityInteraction)
        GlobalEventHandler.Server.register<PlayerBlockInteractionEvent>(priority = Int.MIN_VALUE, listener = ::onPlayerBlockInteraction)
        GlobalEventHandler.Server.register<PlayerBlockStartMiningEvent>(priority = Int.MIN_VALUE, listener = ::onPlayerBlockStartMining)
        GlobalEventHandler.Server.register<PlayerDropItemEvent>(priority = Int.MIN_VALUE, listener = ::onPlayerDropItem)
        GlobalEventHandler.Server.register<PlayerSwapOffhandEvent>(priority = Int.MIN_VALUE, listener = ::onPlayerSwapOffhand)
        GlobalEventHandler.Server.register<PlayerPickBlockEvent>(priority = Int.MIN_VALUE, listener = ::onPlayerPickBlock)
        GlobalEventHandler.Server.register<PlayerPickEntityEvent>(priority = Int.MIN_VALUE, listener = ::onPlayerPickEntity)
    }

    private fun onPlayerClientSwingHand(event: PlayerClientSwingHandEvent) {
        val (player) = event
        val inventory = player.inventory
        if (inventory is VirtualInventory) {
            inventory.interact(inventory.selectedSlot, SlotInteractAction.Swing)
            event.cancel()
        }
    }

    private fun onPlayerTryAttack(event: PlayerTryAttackEvent) {
        val (player, target) = event
        val inventory = player.inventory
        if (inventory is VirtualInventory) {
            if (inventory.interact(inventory.selectedSlot, SlotInteractAction.AttackEntity(target))) {
                event.cancel()
            }
        }
    }

    private fun onPlayerItemUse(event: PlayerItemUseEvent) {
        val (player, _, hand) = event
        val inventory = player.inventory
        if (inventory is VirtualInventory) {
            if (inventory.interact(inventory.getSlot(hand), SlotInteractAction.Use)) {
                event.cancel(InteractionResult.FAIL)
            }
        }
    }

    private fun onPlayerEntityInteraction(event: PlayerEntityInteractionEvent) {
        val (player, target, hand) = event
        val inventory = player.inventory
        if (inventory is VirtualInventory) {
            if (inventory.interact(inventory.getSlot(hand), SlotInteractAction.UseOnEntity(target))) {
                event.cancel(InteractionResult.FAIL)
            }
        }
    }

    private fun onPlayerBlockInteraction(event: PlayerBlockInteractionEvent) {
        val (player, _, hand, result) = event
        val inventory = player.inventory
        if (inventory is VirtualInventory) {
            if (inventory.interact(inventory.getSlot(hand), SlotInteractAction.UseOnBlock(result))) {
                event.cancel(InteractionResult.FAIL)
            }
        }
    }

    private fun onPlayerBlockStartMining(event: PlayerBlockStartMiningEvent) {
        val (player, pos, face) = event
        val inventory = player.inventory
        if (inventory is VirtualInventory) {
            if (inventory.interact(inventory.selectedSlot, SlotInteractAction.AttackBlock(pos, face))) {
                event.cancel()
            }
        }
    }

    private fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val (player, all) = event
        val inventory = player.inventory
        if (inventory is VirtualInventory) {
            if (inventory.interact(inventory.selectedSlot, SlotInteractAction.Drop(all))) {
                event.cancel()
            }
        }
    }

    private fun onPlayerSwapOffhand(event: PlayerSwapOffhandEvent) {
        val (player) = event
        val inventory = player.inventory
        if (inventory is VirtualInventory) {
            if (inventory.interact(inventory.selectedSlot, SlotInteractAction.SwapOffhand)) {
                event.cancel()
            }
        }
    }

    private fun onPlayerPickBlock(event: PlayerPickBlockEvent) {
        val (player, pos, state) = event
        val inventory = player.inventory
        if (inventory is VirtualInventory) {
            inventory.interact(inventory.selectedSlot, SlotInteractAction.PickBlock(pos, state))
            event.cancel()
        }
    }

    private fun onPlayerPickEntity(event: PlayerPickEntityEvent) {
        val (player, entity) = event
        val inventory = player.inventory
        if (inventory is VirtualInventory) {
            inventory.interact(inventory.selectedSlot, SlotInteractAction.PickEntity(entity))
            event.cancel()
        }
    }

    private fun Inventory.getSlot(hand: InteractionHand): Int {
        return if (hand == InteractionHand.MAIN_HAND) this.selectedSlot else Inventory.SLOT_OFFHAND
    }
}