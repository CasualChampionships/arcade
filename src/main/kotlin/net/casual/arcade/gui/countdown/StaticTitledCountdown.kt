package net.casual.arcade.gui.countdown

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.casual.arcade.utils.serialization.ComponentSerializer
import net.minecraft.network.chat.Component

@Serializable
@SerialName("titled")
public class StaticTitledCountdown(
    @Serializable(with = ComponentSerializer::class)
    private val title: Component = TitledCountdown.DEFAULT_TITLE
): TitledCountdown {
    override fun getCountdownTitle(current: Int): Component {
        return this.title
    }
}