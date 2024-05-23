package net.casual.arcade.gui.bossbar.templates

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.utils.registries.ArcadeRegistries
import net.casual.arcade.utils.serialization.CodecProvider.Companion.register
import net.minecraft.core.Registry
import java.util.function.Function

public interface TimerBossBarTemplate {
    public fun create(): TimerBossBar

    public fun codec(): MapCodec<out TimerBossBarTemplate>

    public companion object {
        public val DEFAULT: SimpleTimerBossbarTemplate = SimpleTimerBossbarTemplate()

        public val CODEC: Codec<TimerBossBarTemplate> by lazy {
            ArcadeRegistries.TIMER_BOSSBAR_TEMPLATE.byNameCodec()
                .dispatch(TimerBossBarTemplate::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out TimerBossBarTemplate>>) {
            SimpleTimerBossbarTemplate.register(registry)
        }
    }
}