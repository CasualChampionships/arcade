package net.casualuhc.arcade.recipes

import net.minecraft.core.NonNullList
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.item.crafting.ShapelessRecipe

@Suppress("unused", "MemberVisibilityCanBePrivate")
object CraftingRecipeBuilder {
    fun shaped(block: Shaped.() -> Unit): Shaped {
        return Shaped().apply(block)
    }

    fun shapeless(block: Shapeless.() -> Unit): Shapeless {
        return Shapeless().apply(block)
    }

    class Shaped internal constructor() {
        private val ingredients = NonNullList.create<Ingredient>()
        var id: ResourceLocation? = null
        var category: CraftingBookCategory = CraftingBookCategory.MISC
        var group: String = ""
        var width: Int = 0
        var height: Int = 0
        var result: ItemStack = ItemStack.EMPTY

        fun id(id: ResourceLocation): Shaped {
            this.id = id
            return this
        }

        fun category(category: CraftingBookCategory): Shaped {
            this.category = category
            return this
        }

        fun group(group: String): Shaped {
            this.group = group
            return this
        }

        fun ingredient(ingredient: Ingredient): Shaped {
            this.ingredients.add(ingredient)
            return this
        }

        fun ingredient(item: Item): Shaped {
            return this.ingredient(Ingredient.of(item))
        }

        fun ingredient(item: TagKey<Item>): Shaped {
            return this.ingredient(Ingredient.of(item))
        }

        fun build(): ShapedRecipe {
            val id = this.id
            requireNotNull(id)
            return ShapedRecipe(
                id,
                this.group,
                this.category,
                this.width,
                this.height,
                this.ingredients,
                this.result
            )
        }
    }

    class Shapeless internal constructor() {
        private val ingredients = NonNullList.create<Ingredient>()
        var id: ResourceLocation? = null
        var category: CraftingBookCategory = CraftingBookCategory.MISC
        var group: String = ""
        var result: ItemStack = ItemStack.EMPTY

        fun id(id: ResourceLocation): Shapeless {
            this.id = id
            return this
        }

        fun category(category: CraftingBookCategory): Shapeless {
            this.category = category
            return this
        }

        fun group(group: String): Shapeless {
            this.group = group
            return this
        }

        fun ingredient(ingredient: Ingredient): Shapeless {
            this.ingredients.add(ingredient)
            return this
        }

        fun ingredient(item: Item): Shapeless {
            return this.ingredient(Ingredient.of(item))
        }

        fun ingredient(item: TagKey<Item>): Shapeless {
            return this.ingredient(Ingredient.of(item))
        }

        fun build(): ShapelessRecipe {
            val id = this.id
            requireNotNull(id)
            return ShapelessRecipe(
                id,
                this.group,
                this.category,
                this.result,
                this.ingredients,
            )
        }
    }
}