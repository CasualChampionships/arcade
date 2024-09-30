package net.casual.arcade.test

import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.spawner.WanderingTraderSpawnerFactory
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevelsBuilder
import net.casual.arcade.dimensions.utils.loadCustomLevel
import net.casual.arcade.dimensions.utils.loadOrAddCustomLevel
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.SingleListenerProvider
import net.casual.arcade.events.registry.RegistryEventHandler
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.utils.MathUtils
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.set
import net.casual.arcade.visuals.nametag.PlayerNameTag
import net.casual.arcade.visuals.screen.SelectionGuiBuilder
import net.casual.arcade.visuals.screen.SelectionGuiComponents
import net.casual.arcade.visuals.sidebar.Sidebar
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.dimension.LevelStem

object ArcadeTest: ModInitializer {
    override fun onInitialize() {
        Minigames.registerFactory(TestMinigame.ID) { context ->
            val server = context.server
            val levels = VanillaLikeLevelsBuilder.build(server) {
                set(VanillaDimension.Overworld) {
                    dimensionKey(ResourceUtils.random { "overworld_$it" })
                    weather {
                        rainTime = 20.Minutes.ticks
                        raining = true
                    }
                    persistence = LevelPersistence.Temporary
                }
                set(VanillaDimension.Nether) {
                    dimensionKey(ResourceUtils.random { "the_nether_$it" })
                    persistence = LevelPersistence.Temporary
                }
            }

            val minigame = TestMinigame(server)
            minigame.levels.addAll(levels.all())
            minigame
        }
    }
}

object Example: ModInitializer {
    override fun onInitialize() = GlobalEventHandler.register<ServerLoadedEvent> { (server) ->
        val key = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.withDefaultNamespace("foo")
        )

        val builder = VanillaLikeLevelsBuilder()
            .set(VanillaDimension.Overworld) {
                randomDimensionKey()
                levelStem(LevelStem.NETHER)
                customSpawners()
            }
        builder.build(server)

        RegistryEventHandler.register(Registries.DIMENSION_TYPE) { (reg) ->
            Registry.register(reg, ResourceLocation.withDefaultNamespace("foo"), DimensionType(/* */))
        }
        SelectionGuiBuilder(null as ServerPlayer, null as SelectionGuiComponents)
            .components {

            }
        PlayerNameTag({ Component.literal("") })
        MathUtils.centeredScale()
    }
}