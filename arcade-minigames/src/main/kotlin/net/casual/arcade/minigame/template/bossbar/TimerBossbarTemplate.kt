package net.casual.arcade.minigame.template.bossbar

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.casual.arcade.visuals.bossbar.TimerBossbar
import net.minecraft.core.Registry
import java.util.function.Function

public interface TimerBossbarTemplate {
    public fun create(): TimerBossbar

    public fun codec(): MapCodec<out TimerBossbarTemplate>

    public companion object {
        public val DEFAULT: SimpleTimerBossbarTemplate = SimpleTimerBossbarTemplate()

        public val CODEC: Codec<TimerBossbarTemplate> by lazy {
            MinigameRegistries.TIMER_BOSSBAR_TEMPLATE.byNameCodec()
                .dispatch(TimerBossbarTemplate::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out TimerBossbarTemplate>>) {
            SimpleTimerBossbarTemplate.register(registry)
        }
    }
}