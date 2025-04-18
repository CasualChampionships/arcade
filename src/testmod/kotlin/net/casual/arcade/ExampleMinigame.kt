package net.casual.arcade

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.serialization.MapCodec
import eu.pb4.sgui.api.gui.GuiInterface
import net.casual.arcade.commands.*
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.commands.type.CustomArgumentTypeInfo
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.dimensions.utils.addCustomLevel
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.events.MinigameInitializeEvent
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.settings.GameSetting
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.settings.display.DisplayableSettingsDefaults
import net.casual.arcade.minigame.settings.display.MenuGameSetting
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder
import net.casual.arcade.minigame.task.impl.PhaseChangeTask
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.asLocation
import net.casual.arcade.utils.teleportTo
import net.casual.arcade.visuals.screen.SelectionGuiBuilder
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Items
import java.util.*
import java.util.concurrent.CompletableFuture

enum class ExamplePhases(
    override val id: String
): Phase<ExampleMinigame> {
    Grace("grace") {
        override fun start(minigame: ExampleMinigame, previous: Phase<ExampleMinigame>) {
            minigame.settings.canPvp.set(false)

            // In 10 minutes we will move to the next phase
            minigame.scheduler.schedulePhased(10.Minutes, PhaseChangeTask(minigame, Active))
        }
    },
    Active("active") {
        override fun start(minigame: ExampleMinigame, previous: Phase<ExampleMinigame>) {
            minigame.settings.canPvp.set(true)

            // In 30 minutes we will move to the next phase
            minigame.scheduler.schedulePhased(30.Minutes, PhaseChangeTask(minigame, DeathMatch))
        }
    },
    DeathMatch("death_match") {
        override fun start(minigame: ExampleMinigame, previous: Phase<ExampleMinigame>) {
            // Change to location of the arena
            val location = minigame.server.overworld().asLocation()
            for (player in minigame.players.playing) {
                player.teleportTo(location)
            }
        }
    }
}

class ExampleMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid) {
    override val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath("modid", "example")

    override fun phases(): Collection<Phase<ExampleMinigame>> {
        return listOf(ExamplePhases.Grace, ExamplePhases.Active, ExamplePhases.DeathMatch)
    }

    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        // Add a level which our minigame will handle
        this.levels.add(CustomLevelBuilder.build(this.server) {
            randomDimensionKey()
            vanillaDefaults(VanillaDimension.Overworld)
        })

        // Add a level which we must handle
        val level = this.server.addCustomLevel {
            randomDimensionKey()
            vanillaDefaults(VanillaDimension.Overworld)
        }
        this.levels.add(level)

        val minigame: Minigame = this
    }
}

class ExampleSettings(minigame: Minigame): MinigameSettings(minigame) {
    val myCustomSetting: GameSetting<Int> = this.register(MenuGameSettingBuilder.int32 {
        name = "my_setting"
        value = 100
        display = Items.IRON_BLOCK.named("My Setting")

        option("first_option", Items.OAK_PLANKS.named("First Option"), 0)
        option("second_option", Items.BIRCH_PLANKS.named("Second Option"), 50)
        option("third_option", Items.SPRUCE_PLANKS.named("Third Option"), 100)

        listener { setting: GameSetting<Int>, previous: Int, value: Int ->
            println("My Setting was set to $value")
        }

        override = { player: ServerPlayer ->
            player.experienceLevel
        }
    })
}

class CustomSettingsDefaults: DisplayableSettingsDefaults() {
    override fun createSettingsGuiBuilder(player: ServerPlayer): SelectionGuiBuilder {
        return super.createSettingsGuiBuilder(player)
    }

    override fun createOptionsGuiBuilder(parent: GuiInterface, setting: MenuGameSetting<*>): SelectionGuiBuilder {
        return super.createOptionsGuiBuilder(parent, setting)
    }
}

object ExampleMinigameFactory: MinigameFactory {
    private val codec = MapCodec.unit(this)

    override fun create(context: MinigameCreationContext): Minigame {
        return ExampleMinigame(context.server, context.uuid)
    }

    override fun codec(): MapCodec<out MinigameFactory> {
        return this.codec
    }
}

object ExampleMinigameMod: ModInitializer {
    override fun onInitialize() {
        Registry.register(
            MinigameRegistries.MINIGAME_FACTORY,
            ResourceLocation.fromNamespaceAndPath("modid", "example"),
            ExampleMinigameFactory.codec()
        )
    }
}


class ExampleArgumentType: CustomArgumentType<String>() {
    override fun parse(reader: StringReader): String {
        TODO("Not yet implemented")
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return super.listSuggestions(context, builder)
    }

    override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return super.getArgumentInfo()
    }
}

fun sendMyHiddenCommand(player: ServerPlayer) {
    player.sendSystemMessage(
        Component.literal("[CLICK HERE]").singleUseFunction {
            println("Player ${it.player.scoreboardName} clicked the message!")
        }
    )
}