package net.casual.arcade.gui.countdown.templates

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.utils.registries.ArcadeRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import java.util.function.Function

public interface CountdownTemplate {
    public fun create(): Countdown

    public fun codec(): MapCodec<out CountdownTemplate>

    public companion object {
        public val DEFAULT: TitledCountdownTemplate = TitledCountdownTemplate()

        public val CODEC: Codec<CountdownTemplate> by lazy {
            ArcadeRegistries.COUNTDOWN_TEMPLATE.byNameCodec()
                .dispatch(CountdownTemplate::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out CountdownTemplate>>) {
            TitledCountdownTemplate.register(registry)
        }
    }
}
