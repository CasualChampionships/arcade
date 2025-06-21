package net.casual.arcade.test

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.events.MinigameInitializeEvent
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.utils.ComponentUtils.blue
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.italicise
import net.casual.arcade.utils.ComponentUtils.shadowless
import net.casual.arcade.utils.ComponentUtils.white
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.recipe.CraftingRecipeBuilder
import net.casual.arcade.visuals.elements.ComponentElements
import net.casual.arcade.visuals.elements.SidebarElements
import net.casual.arcade.visuals.sidebar.FixedSidebar
import net.casual.arcade.visuals.sidebar.SidebarComponent
import net.casual.arcade.visuals.tab.PlayerListDisplay
import net.casual.arcade.visuals.tab.PlayerListEntries
import net.casual.arcade.visuals.tab.VanillaPlayerListEntries
import net.minecraft.network.chat.Component
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
        this.players.keepPlayerData = false

        this.recipes.add(CraftingRecipeBuilder.shapeless(this.server.registryAccess()) {
            key(ResourceUtils.arcade("example"))
            ingredients(Items.ITEM_FRAME, Items.BLACK_DYE)
            result(ItemStack(Items.NETHERITE_BLOCK))
        })

        val sidebar = FixedSidebar(ComponentElements.of(Component.literal("Example!")))
        sidebar.addRow { player ->
            val delta = (player.levelServer.tickCount % 200 / 2.0F) - 50F
            val c = Component.empty()
                .append("□")
                .append(SpacingFontResources.spaced(2))
                .append("□")
                .append(SpacingFontResources.spaced(10))
                .append("□")
                .append(" ")
                .append("□")
                .append(SpacingFontResources.composed(-14.178993F))
                .append("□")
                .append(SpacingFontResources.composed(delta))
                .append(Component.literal("■").blue())
            SidebarComponent.withNoScore(c)
        }
        sidebar.addRow(SidebarElements.withNoScore(SpacingFontResources.spaced(120)))
        this.ui.setSidebar(sidebar)

        val display = PlayerListDisplay(VanillaPlayerListEntries())
        display.setDisplay(
            { player -> Component.literal("Testing Minigame\n").blue()
                .append(Component.literal("shadowless").shadowless().italicise().bold().white()) },
            { player -> Component.empty() }
        )
        this.ui.setPlayerListDisplay(display)
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