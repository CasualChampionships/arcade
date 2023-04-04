package net.casualuhc.arcade.recipes

import net.minecraft.core.RegistryAccess
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.level.Level

@Deprecated("Rework needed")
object RecipeFactory {
    @Deprecated("Rework needed")
    fun createCrafting(
        id: ResourceLocation,
        category: CraftingBookCategory,
        grid: RecipeGrid,
        result: (CraftingContainer) -> ItemStack
    ): CustomRecipe {
        if (!grid.complete()) {
            throw IllegalArgumentException("Tried to create recipe with non-complete grid")
        }
        return object: CustomRecipe(id, category) {
            override fun matches(container: CraftingContainer, level: Level): Boolean {
                if (!this.canCraftInDimensions(container.width, container.height)) {
                    return false
                }
                val items = ArrayList<ItemStack>(container.width * container.height)
                for (i in 0 until container.width * container.height) {
                    items.add(container.getItem(i))
                }
                return grid.matches(items)
            }

            override fun assemble(container: CraftingContainer, registryAccess: RegistryAccess): ItemStack {
                return result(container)
            }

            override fun canCraftInDimensions(width: Int, height: Int): Boolean {
                return width == grid.width && height == grid.height
            }

            override fun getSerializer(): RecipeSerializer<*> {
                return ArcadeRecipeSerializer
            }
        }
    }
}