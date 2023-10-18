package net.casual.arcade.recipes

import net.minecraft.core.NonNullList
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.item.crafting.ShapelessRecipe
import java.util.function.Predicate

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
        public var canPlayerUse: Predicate<ServerPlayer> = Predicate { _ -> true }

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

        public fun canPlayerUse(canPlayerUse: Predicate<ServerPlayer>): Shaped {
            this.canPlayerUse = canPlayerUse
            return this
        }

        public fun build(): ShapedRecipe {
            val id = this.id
            requireNotNull(id)
            return object: ShapedRecipe(
                id,
                this.group,
                this.category,
                this.width,
                this.height,
                this.ingredients,
                this.result
            ), PlayerPredicatedRecipe {
                override fun canUse(player: ServerPlayer): Boolean {
                    return canPlayerUse.test(player)
                }
            }
        }
    }

    public class Shapeless internal constructor() {
        private val ingredients = NonNullList.create<Ingredient>()
        public var id: ResourceLocation? = null
        public var category: CraftingBookCategory = CraftingBookCategory.MISC
        public var group: String = ""
        public var result: ItemStack = ItemStack.EMPTY
        public var canPlayerUse: Predicate<ServerPlayer> = Predicate { _ -> true }

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

        public fun canPlayerUse(canPlayerUse: Predicate<ServerPlayer>): Shapeless {
            this.canPlayerUse = canPlayerUse
            return this
        }

        public fun build(): ShapelessRecipe {
            val id = this.id
            requireNotNull(id)
            return object: ShapelessRecipe(
                id,
                this.group,
                this.category,
                this.result,
                this.ingredients,
            ), PlayerPredicatedRecipe {
                override fun canUse(player: ServerPlayer): Boolean {
                    return canPlayerUse.test(player)
                }
            }
        }
    }
}