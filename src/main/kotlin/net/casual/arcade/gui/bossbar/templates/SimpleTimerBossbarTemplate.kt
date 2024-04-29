package net.casual.arcade.gui.bossbar.templates

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.utils.CodecUtils.encodedOptionalFieldOf
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.TimeUtils.formatHHMMSS
import net.casual.arcade.utils.serialization.ArcadeExtraCodecs
import net.casual.arcade.utils.serialization.CodecProvider
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

public class SimpleTimerBossbarTemplate(
    private val title: String = DEFAULT_TITLE,
    private val overlay: BossBarOverlay = BossBarOverlay.PROGRESS,
    private val colour: BossBarColor = BossBarColor.YELLOW,
): TimerBossBarTemplate {
    override fun create(): TimerBossBar {
        return object: TimerBossBar() {
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

    override fun codec(): Codec<out TimerBossBarTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<SimpleTimerBossbarTemplate> {
        private const val DEFAULT_TITLE = "Starting In: %s"

        override val ID: ResourceLocation = Arcade.id("simple")

        override val CODEC: Codec<out SimpleTimerBossbarTemplate> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.encodedOptionalFieldOf("title", DEFAULT_TITLE).forGetter(SimpleTimerBossbarTemplate::title),
                ArcadeExtraCodecs.enum<BossBarOverlay>().encodedOptionalFieldOf("overlay", BossBarOverlay.PROGRESS).forGetter(SimpleTimerBossbarTemplate::overlay),
                ArcadeExtraCodecs.enum<BossBarColor>().encodedOptionalFieldOf("colour", BossBarColor.YELLOW).forGetter(SimpleTimerBossbarTemplate::colour),
            ).apply(instance, ::SimpleTimerBossbarTemplate)
        }

    }
}