package net.casual.arcade.items

import net.minecraft.world.item.ItemStack

class HashableItemStack(val stack: ItemStack) {
    override fun equals(other: Any?): Boolean {
        if (other !is HashableItemStack) {
            return false
        }
        return ItemStack.isSameItemSameTags(this.stack, other.stack)
    }

    override fun hashCode(): Int {
        val hash = 31 * this.stack.item.hashCode()
        val tag = this.stack.tag ?: return hash
        return 31 * (hash + tag.hashCode())
    }
}