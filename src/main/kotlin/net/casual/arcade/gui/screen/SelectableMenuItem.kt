package net.casual.arcade.gui.screen

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

public abstract class SelectableMenuItem {
    public abstract val default: ItemStack

    public abstract fun selected(player: ServerPlayer)

    public open fun shouldUpdate(player: ServerPlayer): Boolean {
        return false
    }

    public open fun update(stack: ItemStack, player: ServerPlayer): ItemStack {
        return this.default
    }

    public class Builder internal constructor() {
        public var default: ItemStack = ItemStack.EMPTY
        public var onSelected: (ServerPlayer) -> Unit = { }
        public var onUpdate: ((ItemStack, ServerPlayer) -> ItemStack)? = null

        public fun build(): SelectableMenuItem {
            val stack = this.default
            val selected = this.onSelected
            val shouldUpdate = this.onUpdate != null
            val update = this.onUpdate
            return object: SelectableMenuItem() {
                override val default: ItemStack = stack

                override fun selected(player: ServerPlayer) {
                    selected.invoke(player)
                }

                override fun shouldUpdate(player: ServerPlayer): Boolean {
                    return shouldUpdate
                }

                override fun update(stack: ItemStack, player: ServerPlayer): ItemStack {
                    return update?.invoke(stack, player) ?: super.update(stack, player)
                }
            }
        }
    }

    public companion object {
        public fun of(stack: ItemStack, action: (ServerPlayer) -> Unit): SelectableMenuItem {
            return build {
                default = stack
                onSelected = action
            }
        }

        public fun build(block: Builder.() -> Unit): SelectableMenuItem {
            val builder = Builder()
            block(builder)
            return builder.build()
        }
    }
}