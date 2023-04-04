package net.casualuhc.arcade.recipes

import com.google.common.base.Predicates
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

@Deprecated("Rework needed")
class RecipeGrid(
    val width: Int,
    val height: Int
) {
    private val predicates = Array<Predicate<ItemStack>>(this.width * this.height) { Predicates.alwaysFalse() }
    private var index = 0

    fun next(predicate: Predicate<ItemStack>): RecipeGrid {
        if (this.complete()) {
            throw IllegalStateException("Grid exceeded max size")
        }
        this.predicates[this.index++] = predicate
        return this
    }

    fun next(item: Item): RecipeGrid {
        return this.next { it.`is`(item) }
    }

    fun matches(items: List<ItemStack>): Boolean {
        if (!this.complete() || (items.size != this.width * this.height)) {
            return false
        }
        items.forEachIndexed { i, stack ->
            if (!this.predicates[i].test(stack)) {
                return false
            }
        }
        return true
    }

    fun complete(): Boolean {
        return this.width * this.height == this.index
    }
}