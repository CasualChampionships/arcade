package net.casual.arcade.test

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.events.MinigameInitializeEvent
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.recipe.CraftingRecipeBuilder
import net.casual.arcade.visuals.elements.ComponentElements
import net.casual.arcade.visuals.sidebar.DynamicSidebar
import net.casual.arcade.visuals.sidebar.SidebarComponent
import net.casual.arcade.visuals.sidebar.SidebarComponents
import net.casual.arcade.visuals.sidebar.SidebarComponents.Companion.addRow
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.*

enum class TestPhase(override val id: String): Phase<TestMinigame> {
    First("first"),
    Second("second")
}

open class TestMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid) {
    override val id: ResourceLocation get() = ID

    override fun phases(): Collection<Phase<out Minigame>> {
        return TestPhase.entries
    }

    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        this.recipes.add(CraftingRecipeBuilder.shapeless(this.server.registryAccess()) {
            key(ResourceUtils.arcade("example"))
            ingredients(Items.ITEM_FRAME, Items.BLACK_DYE)
            result(ItemStack(Items.NETHERITE_BLOCK))
        })

        val sidebar = DynamicSidebar(ComponentElements.of("Example!".literal()))
        sidebar.setRows { player ->
            val components = SidebarComponents.of(
                SidebarComponent.EMPTY,
                SidebarComponent.withNoScore(player.displayName!!)
            )

            if (player.isCreative) {
                components.addRow("Creative".literal(), "true".literal().red())
                components.addRow("Some random text".literal())
            }

            components
        }
        this.ui.setSidebar(sidebar)
    }

    companion object: MinigameFactory {
        val ID = ResourceUtils.arcade("test_minigame")

        override fun create(context: MinigameCreationContext): Minigame {
            return ChildTestMinigame(context.server, context.uuid)
        }

        override fun codec(): MapCodec<out MinigameFactory> {
            return MapCodec.unit(this)
        }
    }
}

class ChildTestMinigame(server: MinecraftServer, uuid: UUID): TestMinigame(server, uuid) {

}