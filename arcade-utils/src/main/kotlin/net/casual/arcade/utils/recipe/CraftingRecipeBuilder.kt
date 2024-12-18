/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.recipe

import net.minecraft.core.HolderLookup.Provider
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.*
import java.util.*

public object CraftingRecipeBuilder {
    public fun shaped(provider: Provider = RegistryAccess.EMPTY, block: Shaped.() -> Unit): RecipeHolder<ShapedRecipe> {
        return Shaped(provider).apply(block).build()
    }

    public fun shapeless(provider: Provider = RegistryAccess.EMPTY, block: Shapeless.() -> Unit): RecipeHolder<ShapelessRecipe> {
        return Shapeless(provider).apply(block).build()
    }

    public class Shaped internal constructor(private val provider: Provider) {
        private val lookup by lazy {
            this.provider.lookup(Registries.ITEM).orElseThrow {
                IllegalArgumentException("RecipeBuilder must be provided with lookup")
            }
        }

        private val ingredients = ArrayList<Optional<Ingredient>>()
        public var key: ResourceKey<Recipe<*>>? = null
        public var category: CraftingBookCategory = CraftingBookCategory.MISC
        public var group: String = ""
        public var width: Int = 0
        public var height: Int = 0
        public var result: ItemStack = ItemStack.EMPTY

        public fun key(id: ResourceLocation): Shaped {
            this.key = ResourceKey.create(Registries.RECIPE, id)
            return this
        }

        public fun key(key: ResourceKey<Recipe<*>>): Shaped {
            this.key = key
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

        public fun ingredients(vararg ingredients: Ingredient): Shaped {
            this.ingredients.addAll(ingredients.map { Optional.of(it) })
            return this
        }

        public fun ingredients(vararg items: Item): Shaped {
            this.ingredients(*items.map { Ingredient.of(it) }.toTypedArray())
            return this
        }

        public fun ingredients(vararg tags: TagKey<Item>): Shaped {
            this.ingredients(*tags.map { Ingredient.of(this.lookup.getOrThrow(it)) }.toTypedArray())
            return this
        }

        public fun result(stack: ItemStack): Shaped {
            this.result = stack
            return this
        }

        public fun build(): RecipeHolder<ShapedRecipe> {
            val id = requireNotNull(this.key)
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

    public class Shapeless internal constructor(private val provider: Provider) {
        private val lookup by lazy {
            this.provider.lookup(Registries.ITEM).orElseThrow {
                IllegalArgumentException("RecipeBuilder must be provided with lookup")
            }
        }

        private val ingredients = ArrayList<Ingredient>()
        public var key: ResourceKey<Recipe<*>>? = null
        public var category: CraftingBookCategory = CraftingBookCategory.MISC
        public var group: String = ""
        public var result: ItemStack = ItemStack.EMPTY

        public fun key(id: ResourceLocation): Shapeless {
            this.key = ResourceKey.create(Registries.RECIPE, id)
            return this
        }

        public fun key(key: ResourceKey<Recipe<*>>): Shapeless {
            this.key = key
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

        public fun ingredients(vararg ingredients: Ingredient): Shapeless {
            this.ingredients.addAll(ingredients)
            return this
        }

        public fun ingredients(vararg items: Item): Shapeless {
            this.ingredients(*items.map { Ingredient.of(it) }.toTypedArray())
            return this
        }

        public fun ingredients(vararg tags: TagKey<Item>): Shapeless {
            this.ingredients(*tags.map { Ingredient.of(this.lookup.getOrThrow(it)) }.toTypedArray())
            return this
        }

        public fun result(stack: ItemStack): Shapeless {
            this.result = stack
            return this
        }

        public fun build(): RecipeHolder<ShapelessRecipe> {
            val id = requireNotNull(this.key)
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