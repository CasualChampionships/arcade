package net.casual.arcade.recipes

import net.minecraft.core.NonNullList
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.item.crafting.ShapelessRecipe

public object CraftingRecipeBuilder {
    public fun shaped(block: Shaped.() -> Unit): Shaped {
        return Shaped().apply(block)
    }

    public fun shapeless(block: Shapeless.() -> Unit): Shapeless {
        return Shapeless().apply(block)
    }

    public class Shaped internal constructor() {
        private val ingredients = NonNullList.create<Ingredient>()
        public var id: ResourceLocation? = null
        public var category: CraftingBookCategory = CraftingBookCategory.MISC
        public var group: String = ""
        public var width: Int = 0
        public var height: Int = 0
        public var result: ItemStack = ItemStack.EMPTY

        public fun id(id: ResourceLocation): Shaped {
            this.id = id
            return this
        }

        public fun category(category: CraftingBookCategory): Shaped {
            this.category = category
            return this
        }

        public fun group(group: String): Shaped {
            this.group = group
            return this
        }

        public fun ingredient(ingredient: Ingredient): Shaped {
            this.ingredients.add(ingredient)
            return this
        }

        public fun ingredient(item: Item): Shaped {
            return this.ingredient(Ingredient.of(item))
        }

        public fun ingredient(item: TagKey<Item>): Shaped {
            return this.ingredient(Ingredient.of(item))
        }

        public fun build(): ShapedRecipe {
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

    public class Shapeless internal constructor() {
        private val ingredients = NonNullList.create<Ingredient>()
        public var id: ResourceLocation? = null
        public var category: CraftingBookCategory = CraftingBookCategory.MISC
        public var group: String = ""
        public var result: ItemStack = ItemStack.EMPTY

        public fun id(id: ResourceLocation): Shapeless {
            this.id = id
            return this
        }

        public fun category(category: CraftingBookCategory): Shapeless {
            this.category = category
            return this
        }

        public fun group(group: String): Shapeless {
            this.group = group
            return this
        }

        public fun ingredient(ingredient: Ingredient): Shapeless {
            this.ingredients.add(ingredient)
            return this
        }

        public fun ingredient(item: Item): Shapeless {
            return this.ingredient(Ingredient.of(item))
        }

        public fun ingredient(item: TagKey<Item>): Shapeless {
            return this.ingredient(Ingredient.of(item))
        }

        public fun build(): ShapelessRecipe {
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