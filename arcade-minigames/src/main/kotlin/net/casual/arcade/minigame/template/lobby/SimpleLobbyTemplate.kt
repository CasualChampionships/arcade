package net.casual.arcade.minigame.template.lobby

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.area.PlaceableArea
import net.casual.arcade.minigame.lobby.Lobby
import net.casual.arcade.minigame.template.area.PlaceableAreaTemplate
import net.casual.arcade.minigame.template.bossbar.TimerBossbarTemplate
import net.casual.arcade.minigame.template.countdown.CountdownTemplate
import net.casual.arcade.minigame.template.location.ExactLocationTemplate
import net.casual.arcade.minigame.template.location.LocationTemplate
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.encodedOptionalFieldOf
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.visuals.bossbar.TimerBossbar
import net.casual.arcade.visuals.countdown.Countdown
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

public open class SimpleLobbyTemplate(
    public val area: PlaceableAreaTemplate = PlaceableAreaTemplate.DEFAULT,
    public val spawn: LocationTemplate = DEFAULT_SPAWN,
    public val countdown: CountdownTemplate = CountdownTemplate.DEFAULT,
    public val bossbar: TimerBossbarTemplate = TimerBossbarTemplate.DEFAULT
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

            override fun createBossbar(): TimerBossbar {
                return bossbar.create()
            }
        }
    }

    override fun codec(): MapCodec<out LobbyTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<SimpleLobbyTemplate> {
        private val DEFAULT_SPAWN = ExactLocationTemplate(Vec3(0.0, 1.0, 0.0))

        override val ID: ResourceLocation = ResourceUtils.arcade("simple")

        override val CODEC: MapCodec<SimpleLobbyTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                PlaceableAreaTemplate.CODEC.encodedOptionalFieldOf("area", PlaceableAreaTemplate.DEFAULT).forGetter(SimpleLobbyTemplate::area),
                LocationTemplate.CODEC.encodedOptionalFieldOf("spawn", DEFAULT_SPAWN).forGetter(SimpleLobbyTemplate::spawn),
                CountdownTemplate.CODEC.encodedOptionalFieldOf("countdown", CountdownTemplate.DEFAULT).forGetter(SimpleLobbyTemplate::countdown),
                TimerBossbarTemplate.CODEC.encodedOptionalFieldOf("bossbar", TimerBossbarTemplate.DEFAULT).forGetter(SimpleLobbyTemplate::bossbar)
            ).apply(instance, ::SimpleLobbyTemplate)
        }
    }
}