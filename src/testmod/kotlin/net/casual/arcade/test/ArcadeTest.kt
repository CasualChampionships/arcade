package net.casual.arcade.test

import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevelsBuilder
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.TimeUtils.Minutes
import net.fabricmc.api.ModInitializer

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