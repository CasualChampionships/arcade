package net.casual.arcade.minigame.template.countdown

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.encodedOptionalFieldOf
import net.casual.arcade.visuals.countdown.Countdown
import net.casual.arcade.visuals.countdown.TitledCountdown
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.resources.ResourceLocation

public class TitledCountdownTemplate(
    public val title: Component = TitledCountdown.DEFAULT_TITLE
): CountdownTemplate {
    override fun create(): Countdown {
        return TitledCountdown.titled(this.title)
    }

    override fun codec(): MapCodec<out CountdownTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<TitledCountdownTemplate> {
        override val ID: ResourceLocation = ResourceUtils.arcade("titled")

        override val CODEC: MapCodec<TitledCountdownTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ComponentSerialization.CODEC.encodedOptionalFieldOf("title", TitledCountdown.DEFAULT_TITLE).forGetter(TitledCountdownTemplate::title)
            ).apply(instance, ::TitledCountdownTemplate)
        }
    }
}