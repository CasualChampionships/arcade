package net.casual.arcade.gui.countdown.templates

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.TitledCountdown
import net.casual.arcade.utils.CodecUtils.encodedOptionalFieldOf
import net.casual.arcade.utils.serialization.ArcadeExtraCodecs
import net.casual.arcade.utils.serialization.CodecProvider
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.resources.ResourceLocation

public class TitledCountdownTemplate(
    public val title: Component = TitledCountdown.DEFAULT_TITLE
): CountdownTemplate {
    override fun create(): Countdown {
        return TitledCountdown.titled(this.title)
    }

    override fun codec(): Codec<out CountdownTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<TitledCountdownTemplate> {
        override val ID: ResourceLocation = Arcade.id("titled")

        override val CODEC: Codec<TitledCountdownTemplate> = RecordCodecBuilder.create { instance ->
            instance.group(
                ComponentSerialization.CODEC.encodedOptionalFieldOf("title", TitledCountdown.DEFAULT_TITLE).forGetter(TitledCountdownTemplate::title)
            ).apply(instance, ::TitledCountdownTemplate)
        }
    }
}