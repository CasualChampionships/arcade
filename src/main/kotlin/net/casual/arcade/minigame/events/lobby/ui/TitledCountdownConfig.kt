package net.casual.arcade.minigame.events.lobby.ui

import com.google.gson.JsonObject
import net.casual.arcade.gui.countdown.TitledCountdown
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.string

public class TitledCountdownConfig(
    private val title: String
): CountdownConfig {
    override val id: String = TitledCountdownConfig.id
    
    override fun create(): TitledCountdown {
        return TitledCountdown.titled(this.title.literal())
    }

    override fun write(): JsonObject {
        val json = JsonObject()
        json["title"] = this.title
        return json
    }

    public companion object: CountdownConfigFactory {
        override val id: String = "titled"

        override fun create(data: JsonObject): CountdownConfig {
            val title = data.string("title")
            return TitledCountdownConfig(title)
        }
    }
}