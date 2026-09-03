package net.casual.arcade.tests.manual.minigame

import com.mojang.serialization.MapCodec
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigameInitializeEvent
import net.casual.arcade.minigame.managers.MinigameLevelManager.LevelOwnership
import net.casual.arcade.minigame.phase.MinigamePhase
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.serialization.SerializableMinigame
import net.casual.arcade.minigame.settings.GameSetting
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.settings.GameSettingBuilder
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.pack.font.spacing.SpacingFontResources
import net.casual.arcade.pack.utils.spaced
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.component.*
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.player.displayName
import net.casual.arcade.utils.recipe.CraftingRecipeBuilder
import net.casual.arcade.nametags.Nametag
import net.casual.arcade.observer.Observer
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.casual.arcade.virtual.visuals.sidebar.DynamicVirtualSidebar
import net.casual.arcade.virtual.visuals.sidebar.SidebarComponent
import net.casual.arcade.virtual.visuals.tab.DynamicVirtualPlayerList
import net.casual.arcade.virtual.visuals.tab.VanillaPlayerListEntries
import net.casual.arcade.virtual.visuals.utils.elements.ComponentElements
import net.casual.arcade.virtual.visuals.utils.elements.SidebarElements
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import net.minecraft.world.level.levelgen.Heightmap
import java.util.*

enum class TestPhase(override val id: String): MinigamePhase {
    First("first"),
    Second("second")
}

class TestSettings(minigame: Minigame) : MinigameSettings(minigame) {
//    val testEnum: GameSetting<TestPhase> = this.register(MenuGameSettingBuilder.enumeration {
//        name = "test_enum"
//        display = Items.COMPASS.named("Test Enum")
//        value = TestPhase.First
//        defaults.options(this, TestPhase::class.java)
//    })

    val testString: GameSetting<String> = this.register(GameSettingBuilder.string {
        name = "test_string"
        display = Items.NAME_TAG.named("Test String")
        value = "test"
        option("test", Component.literal("Test"), "test")
        option("other", Component.literal("Other"), "other")
        option("third", Component.literal("Third"), "third")
    })

    val testFloat: GameSetting<Float> = this.register(GameSettingBuilder.float32 {
        name = "test_float"
        display = Items.CLOCK.named("Test Float")
        value = 64.0F
    })

    val testInt: GameSetting<Int> = this.register(GameSettingBuilder.int32 {
        name = "test_int"
        display = Items.REPEATER.named("Test Int")
        value = 64
        option("low", Component.literal("Low"), 8)
        option("medium", Component.literal("Medium"), 64)
        option("high", Component.literal("High"), 256)
    })
}

open class TestMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid, ID, TestPhase.entries), SerializableMinigame {
    override val settings: MinigameSettings = TestSettings(this)

    private val level: ServerLevel
        get() = this.levels.require(LEVEL)

    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        this.players.keepPlayerData = false

        val level = CustomLevelBuilder.build(this.server) {
            randomDimensionKey()
            vanillaDefaults(VanillaDimension.Overworld)
            clockState(rate = 5.0F)
            persistence(LevelPersistence.Permanent)
        }
        this.levels.add(LEVEL, level, LevelOwnership.Exclusive)

        this.recipes.add(CraftingRecipeBuilder.shapeless(this.server.registryAccess()) {
            key(arcade("example"))
            ingredients(Items.ITEM_FRAME, Items.DYE.black)
            result(ItemStackTemplate(Items.NETHERITE_BLOCK))
        })

        val sidebar = DynamicVirtualSidebar(this.server)
        sidebar.setTitle(ComponentElements.of(Component.literal("Example!")))
        // Row 0 is the bottom row
        sidebar.setRow(0) { player ->
            SidebarComponent.withCustomScore(
                Component.literal("${player.level().overworldClockTime}"),
                Component.literal("${player.level().defaultClockTime}")
            )
        }
        sidebar.setRow(1, SidebarElements.withNoScore(Component {
            literal("Hello World", 0x2739B8, 0x8D379E, 0xF13484, 0xFF605D)
        }))
        sidebar.setRow(2, SidebarElements.withNoScore(SpacingFontResources.spaced(120)))
        this.visuals.setSidebar(sidebar)

        val display = DynamicVirtualPlayerList(this.server, VanillaPlayerListEntries())
        val header = UniversalElement {
            Component {
                literal("Testing Minigame").blue() + nl + wrap() + list(
                    literal("foo").shadowless().italicize().bold(),
                    translatable("bar").color(0xFF00FF).white(),
                    translatable("baz").strikethrough()
                ).join(spaced(10.0F), suffix = nl) + "123"
            }
        }
        display.setHeader(header)
        display.setFooter(ComponentElements.empty())
        this.visuals.setPlayerListDisplay(display)

        this.visuals.addNametag(object: Nametag {
            override fun getComponent(observee: Entity): Component {
                return if (observee is ServerPlayer) observee.displayName() else observee.name
            }

            override fun isObservable(observee: Entity, observer: Observer): Boolean {
                return true
            }
        })
        this.visuals.addNametag(Nametag.simple(Component.literal("CustomNametags!")))
    }

    @Listener
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        this.level.getChunk(0, 0)
        val y = this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0).toDouble()
        event.player.teleportTo(Location(0.0, y, 0.0, 0.0F, 0.0F).with(this.level))
    }

    override fun factory(): MinigameFactory {
        return TestMinigame
    }

    companion object: MinigameFactory {
        private val CODEC = MapCodec.unit(this)
        val ID = arcade("test_minigame")
        val LEVEL = arcade("level")

        override fun create(context: MinigameCreationContext): Minigame {
            return TestMinigame(context.server, context.uuid)
        }

        override fun codec(): MapCodec<out MinigameFactory> {
            return CODEC
        }
    }
}