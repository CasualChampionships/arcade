package net.casual.arcade.minigame.template.bossbar

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.TimeUtils.formatHHMMSS
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.encodedOptionalFieldOf
import net.casual.arcade.visuals.bossbar.TimerBossbar
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

public class SimpleTimerBossbarTemplate(
    private val title: String = DEFAULT_TITLE,
    private val overlay: BossBarOverlay = BossBarOverlay.PROGRESS,
    private val colour: BossBarColor = BossBarColor.YELLOW,
): TimerBossbarTemplate {
    override fun create(): TimerBossbar {
        return object: TimerBossbar() {
            override fun getTitle(player: ServerPlayer): Component {
                return title.format(this.getRemainingDuration().formatHHMMSS()).literal()
            }

            override fun getColour(player: ServerPlayer): BossBarColor {
                return colour
            }

            override fun getOverlay(player: ServerPlayer): BossBarOverlay {
                return overlay
            }
        }
    }

    override fun codec(): MapCodec<out TimerBossbarTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<SimpleTimerBossbarTemplate> {
        private const val DEFAULT_TITLE = "Starting In: %s"

        override val ID: ResourceLocation = ResourceUtils.arcade("simple")

        override val CODEC: MapCodec<out SimpleTimerBossbarTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.encodedOptionalFieldOf("title", DEFAULT_TITLE).forGetter(SimpleTimerBossbarTemplate::title),
                ArcadeExtraCodecs.enum<BossBarOverlay>().encodedOptionalFieldOf("overlay", BossBarOverlay.PROGRESS).forGetter(SimpleTimerBossbarTemplate::overlay),
                ArcadeExtraCodecs.enum<BossBarColor>().encodedOptionalFieldOf("colour", BossBarColor.YELLOW).forGetter(SimpleTimerBossbarTemplate::colour),
            ).apply(instance, ::SimpleTimerBossbarTemplate)
        }
    }
}