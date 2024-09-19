package net.casual.arcade.minigame.template.countdown

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.casual.arcade.visuals.countdown.Countdown
import net.minecraft.core.Registry
import java.util.function.Function

public interface CountdownTemplate {
    public fun create(): Countdown

    public fun codec(): MapCodec<out CountdownTemplate>

    public companion object {
        public val DEFAULT: TitledCountdownTemplate = TitledCountdownTemplate()

        public val CODEC: Codec<CountdownTemplate> by lazy {
            MinigameRegistries.COUNTDOWN_TEMPLATE.byNameCodec()
                .dispatch(CountdownTemplate::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out CountdownTemplate>>) {
            TitledCountdownTemplate.register(registry)
        }
    }
}
