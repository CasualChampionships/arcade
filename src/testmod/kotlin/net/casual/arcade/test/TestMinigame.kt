package net.casual.arcade.test

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.recipe.CraftingRecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

enum class TestPhase(override val id: String): Phase<TestMinigame> {
    First("first"),
    Second("second")
}

class TestMinigame(server: MinecraftServer): Minigame<TestMinigame>(server) {
    override val id: ResourceLocation get() = ID

    override fun initialize() {
        super.initialize()

        this.recipes.add(CraftingRecipeBuilder.shapeless(this.server.registryAccess()) {
            key(ResourceUtils.arcade("example"))
            ingredients(Items.ITEM_FRAME, Items.BLACK_DYE)
            result(ItemStack(Items.NETHERITE_BLOCK))
        })
    }

    override fun getPhases(): Collection<Phase<TestMinigame>> {
        return TestPhase.entries
    }

    companion object {
        val ID = ResourceUtils.arcade("test_minigame")
    }
}