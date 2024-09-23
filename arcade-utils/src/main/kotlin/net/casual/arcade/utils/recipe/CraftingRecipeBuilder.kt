package net.casual.arcade.utils.recipe

import net.minecraft.core.NonNullList
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.*
import java.util.*

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

        public fun ingredients(vararg ingredient: Ingredient): Shaped {
            this.ingredients.addAll(ingredient)
            return this
        }

        public fun ingredients(vararg item: Item): Shaped {
            this.ingredients.addAll(item.map { Ingredient.of(it) })
            return this
        }

        public fun ingredients(vararg item: TagKey<Item>): Shaped {
            this.ingredients.addAll(item.map(Ingredient::of))
            return this
        }

        public fun build(): RecipeHolder<ShapedRecipe> {
            val id = requireNotNull(this.id)
            val recipe = ShapedRecipe(
                this.group,
                this.category,
                ShapedRecipePattern(
                    this.width,
                    this.height,
                    this.ingredients,
                    Optional.empty()
                ),
                this.result
            )
            return RecipeHolder(id, recipe)
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

        public fun ingredients(vararg ingredient: Ingredient): Shapeless {
            this.ingredients.addAll(ingredient)
            return this
        }

        public fun ingredients(vararg item: Item): Shapeless {
            this.ingredients.addAll(item.map { Ingredient.of(it) })
            return this
        }

        public fun ingredients(vararg item: TagKey<Item>): Shapeless {
            this.ingredients.addAll(item.map(Ingredient::of))
            return this
        }

        public fun build(): RecipeHolder<ShapelessRecipe> {
            val id = requireNotNull(this.id)
            val recipe = ShapelessRecipe(
                this.group,
                this.category,
                this.result,
                this.ingredients,
            )
            return RecipeHolder(id, recipe)
        }
    }
}