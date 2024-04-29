package net.casual.arcade.minigame.events.lobby.templates

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.area.PlaceableArea
import net.casual.arcade.area.templates.PlaceableAreaTemplate
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.templates.CountdownTemplate
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.minigame.events.lobby.LocationConfig
import net.casual.arcade.gui.bossbar.templates.TimerBossBarTemplate
import net.casual.arcade.utils.CodecUtils.encodedOptionalFieldOf
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.utils.serialization.CodecProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel

public open class SimpleLobbyTemplate(
    public val area: PlaceableAreaTemplate = PlaceableAreaTemplate.DEFAULT,
    public val spawn: LocationConfig = LocationConfig.DEFAULT,
    public val countdown: CountdownTemplate = CountdownTemplate.DEFAULT,
    public val bossbar: TimerBossBarTemplate = TimerBossBarTemplate.DEFAULT
): LobbyTemplate {
    override fun create(level: ServerLevel): Lobby {
        val area = this.area.create(level)
        val spawn = this.spawn.toLocation(level)

        return object: Lobby {
            override val area: PlaceableArea = area
            override val spawn: Location = spawn

            override fun getCountdown(): Countdown {
                return countdown.create()
            }

            override fun createBossbar(): TimerBossBar {
                return bossbar.create()
            }
        }
    }

    override fun codec(): Codec<out LobbyTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<SimpleLobbyTemplate> {
        override val ID: ResourceLocation = Arcade.id("simple")

        override val CODEC: Codec<SimpleLobbyTemplate> = RecordCodecBuilder.create { instance ->
            instance.group(
                PlaceableAreaTemplate.CODEC.encodedOptionalFieldOf("area", PlaceableAreaTemplate.DEFAULT).forGetter(SimpleLobbyTemplate::area),
                LocationConfig.CODEC.encodedOptionalFieldOf("spawn", LocationConfig.DEFAULT).forGetter(SimpleLobbyTemplate::spawn),
                CountdownTemplate.CODEC.encodedOptionalFieldOf("countdown", CountdownTemplate.DEFAULT).forGetter(SimpleLobbyTemplate::countdown),
                TimerBossBarTemplate.CODEC.encodedOptionalFieldOf("bossbar", TimerBossBarTemplate.DEFAULT).forGetter(SimpleLobbyTemplate::bossbar)
            ).apply(instance, ::SimpleLobbyTemplate)
        }
    }
}