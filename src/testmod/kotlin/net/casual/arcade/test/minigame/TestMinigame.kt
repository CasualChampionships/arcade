package net.casual.arcade.test.minigame

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.events.MinigameInitializeEvent
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.resources.utils.spaced
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.component.*
import net.casual.arcade.utils.recipe.CraftingRecipeBuilder
import net.casual.arcade.visuals.utils.elements.ComponentElements
import net.casual.arcade.visuals.utils.elements.SidebarElements
import net.casual.arcade.visuals.elements.UniversalElement
import net.casual.arcade.visuals.nametag.PlayerNametag
import net.casual.arcade.visuals.sidebar.FixedSidebar
import net.casual.arcade.visuals.tab.PlayerListDisplay
import net.casual.arcade.visuals.tab.VanillaPlayerListEntries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
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
    override val id: Identifier get() = ID

    override fun phases(): Collection<Phase<out Minigame>> {
        return TestPhase.entries
    }

    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        this.players.keepPlayerData = false

        this.recipes.add(CraftingRecipeBuilder.shapeless(this.server.registryAccess()) {
            key(IdentifierUtils.arcade("example"))
            ingredients(Items.ITEM_FRAME, Items.BLACK_DYE)
            result(ItemStack(Items.NETHERITE_BLOCK))
        })

        val sidebar = FixedSidebar(ComponentElements.of(Component.literal("Example!")))
        sidebar.addRow(SidebarElements.withNoScore(SpacingFontResources.spaced(120)))
        sidebar.addRow(SidebarElements.withNoScore(Component {
            literal("Hello World", 0x2739B8, 0x8D379E, 0xF13484, 0xFF605D)
        }))
        this.visuals.setSidebar(sidebar)

        val display = PlayerListDisplay(VanillaPlayerListEntries())
        val header = UniversalElement {
            Component {
                literal("Testing Minigame").blue() + nl + wrap() + list(
                    literal("foo").shadowless().italicize().bold(),
                    translatable("bar").color(0xFF00FF).white(),
                    translatable("baz").strikethrough()
                ).join(spaced(10.0F), suffix = nl) + "123"
            }
        }
        val footer = ComponentElements.empty()
        display.setDisplay(header, footer)
        this.visuals.setPlayerListDisplay(display)

        this.visuals.addNametag(PlayerNametag.simple({ player -> player.displayName!! }))
        this.visuals.addNametag(PlayerNametag.simple({ Component.literal("CustomNametags!") }))
    }

    override fun factory(): MinigameFactory {
        return TestMinigame
    }

    companion object: MinigameFactory {
        private val CODEC = MapCodec.unit(this)
        val ID = IdentifierUtils.arcade("test_minigame")

        override fun create(context: MinigameCreationContext): Minigame {
            return TestMinigame(context.server, context.uuid)
        }

        override fun codec(): MapCodec<out MinigameFactory> {
            return CODEC
        }
    }
}