package net.casual.arcade.gui.countdown.templates

import com.mojang.serialization.Codec
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.utils.registries.ArcadeRegistries
import net.casual.arcade.utils.serialization.CodecProvider.Companion.register
import net.minecraft.core.Registry
import java.util.function.Function

public interface CountdownTemplate {
    public fun create(): Countdown

    public fun codec(): Codec<out CountdownTemplate>

    public companion object {
        public val DEFAULT: TitledCountdownTemplate = TitledCountdownTemplate()

        public val CODEC: Codec<CountdownTemplate> by lazy {
            ArcadeRegistries.COUNTDOWN_TEMPLATE.byNameCodec()
                .dispatch(CountdownTemplate::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<Codec<out CountdownTemplate>>) {
            TitledCountdownTemplate.register(registry)
        }
    }
}
