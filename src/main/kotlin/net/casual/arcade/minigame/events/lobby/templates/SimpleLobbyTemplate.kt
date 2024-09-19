package net.casual.arcade.minigame.events.lobby.templates

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.area.PlaceableArea
import net.casual.arcade.area.templates.PlaceableAreaTemplate
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.gui.bossbar.templates.TimerBossBarTemplate
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.templates.CountdownTemplate
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.utils.CodecUtils.encodedOptionalFieldOf
import net.casual.arcade.utils.location.Location
import net.casual.arcade.utils.location.template.ExactLocationTemplate
import net.casual.arcade.utils.location.template.LocationTemplate
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

public open class SimpleLobbyTemplate(
    public val area: PlaceableAreaTemplate = PlaceableAreaTemplate.DEFAULT,
    public val spawn: LocationTemplate = DEFAULT_SPAWN,
    public val countdown: CountdownTemplate = CountdownTemplate.DEFAULT,
    public val bossbar: TimerBossBarTemplate = TimerBossBarTemplate.DEFAULT
): LobbyTemplate {
    override fun create(level: ServerLevel): Lobby {
        val area = this.area.create(level)
        val spawnTemplate = this.spawn

        return object: Lobby {
            override val area: PlaceableArea = area
            override val spawn: Location
                get() = spawnTemplate.get(level)

            override fun getCountdown(): Countdown {
                return countdown.create()
            }

            override fun createBossbar(): TimerBossBar {
                return bossbar.create()
            }
        }
    }

    override fun codec(): MapCodec<out LobbyTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<SimpleLobbyTemplate> {
        private val DEFAULT_SPAWN = ExactLocationTemplate(Vec3(0.0, 1.0, 0.0))

        override val ID: ResourceLocation = Arcade.id("simple")

        override val CODEC: MapCodec<SimpleLobbyTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                PlaceableAreaTemplate.CODEC.encodedOptionalFieldOf("area", PlaceableAreaTemplate.DEFAULT).forGetter(SimpleLobbyTemplate::area),
                LocationTemplate.CODEC.encodedOptionalFieldOf("spawn", DEFAULT_SPAWN).forGetter(SimpleLobbyTemplate::spawn),
                CountdownTemplate.CODEC.encodedOptionalFieldOf("countdown", CountdownTemplate.DEFAULT).forGetter(SimpleLobbyTemplate::countdown),
                TimerBossBarTemplate.CODEC.encodedOptionalFieldOf("bossbar", TimerBossBarTemplate.DEFAULT).forGetter(SimpleLobbyTemplate::bossbar)
            ).apply(instance, ::SimpleLobbyTemplate)
        }
    }
}