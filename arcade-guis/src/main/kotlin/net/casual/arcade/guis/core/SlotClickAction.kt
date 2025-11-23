/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.core

import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu.SLOT_CLICKED_OUTSIDE
import net.minecraft.world.inventory.ClickType

private const val NONE: Int = 0
private const val LEFT: Int = 1 shl 0
private const val RIGHT: Int = 1 shl 1
private const val MIDDLE: Int = 1 shl 2
private const val DROP: Int = 1 shl 3
private const val SWAP: Int = 1 shl 4
private const val DRAG: Int = 1 shl 5

public enum class SlotClickAction(
    private val flags: Int
) {
    MouseLeft(LEFT),
    MouseRight(RIGHT),
    MouseMiddle(MIDDLE),
    MouseDoubleClick(NONE),
    MouseLeftShift(LEFT),
    MouseRightShift(RIGHT),
    MouseLeftDragStart(LEFT or DRAG),
    MouseRightDragStart(RIGHT or DRAG),
    MouseMiddleDragStart(MIDDLE or DRAG),
    MouseLeftDragAdd(LEFT or DRAG),
    MouseRightDragAdd(RIGHT or DRAG),
    MouseMiddleDragAdd(MIDDLE or DRAG),
    MouseLeftDragEnd(LEFT or DRAG),
    MouseRightDragEnd(RIGHT or DRAG),
    MouseMiddleDragEnd(MIDDLE or DRAG),
    MouseLeftDrop(LEFT),
    MouseRightDrop(RIGHT),
    Drop(DROP),
    DropAll(DROP),
    SwapSlot1(SWAP),
    SwapSlot2(SWAP),
    SwapSlot3(SWAP),
    SwapSlot4(SWAP),
    SwapSlot5(SWAP),
    SwapSlot6(SWAP),
    SwapSlot7(SWAP),
    SwapSlot8(SWAP),
    SwapSlot9(SWAP),
    SwapOffhand(SWAP),
    Unknown(NONE);
    
    public val isLeft: Boolean get() = this.has(LEFT)
    public val isRight: Boolean get() = this.has(RIGHT)
    public val isMiddle: Boolean get() = this.has(MIDDLE)
    public val isDrop: Boolean get() = this.has(DROP)
    public val isSwap: Boolean get() = this.has(SWAP)
    public val isDrag: Boolean get() = this.has(DRAG)

    private fun has(flag: Int): Boolean {
        return (this.flags and flag) == flag
    }

    public companion object {
        public fun from(type: ClickType, button: Int, slot: Int): SlotClickAction {
            return when (type) {
                ClickType.PICKUP -> if (button == 0) MouseLeft else MouseRight
                ClickType.QUICK_MOVE -> if (button == 0) MouseLeft else MouseRight
                ClickType.SWAP -> when (button) {
                    in 0..<9 -> entries[SwapSlot1.ordinal + button]
                    Inventory.SLOT_OFFHAND -> SwapOffhand
                    else -> Unknown
                }
                ClickType.CLONE -> MouseMiddle
                ClickType.THROW -> when {
                    slot == SLOT_CLICKED_OUTSIDE -> if (button == 0) MouseLeftDrop else MouseRightDrop
                    else -> if (button == 0) Drop else DropAll
                }
                ClickType.QUICK_CRAFT -> when (button) {
                    0 -> MouseLeftDragStart
                    1 -> MouseLeftDragAdd
                    2 -> MouseLeftDragEnd
                    4 -> MouseRightDragStart
                    5 -> MouseRightDragAdd
                    6 -> MouseRightDragEnd
                    8 -> MouseMiddleDragStart
                    9 -> MouseMiddleDragAdd
                    10 -> MouseMiddleDragEnd
                    else -> Unknown
                }
                ClickType.PICKUP_ALL -> MouseDoubleClick
            }
        }
    }
}