package net.casual.arcade.minigame.events.lobby.ui

import com.google.gson.JsonObject
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.stringOrDefault
import net.casual.arcade.utils.TimeUtils.formatHHMMSS
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

public class SimpleTimerBossbarConfig(
    private val title: String,
    private val overlay: BossBarOverlay,
    private val colour: BossBarColor
): TimerBossbarConfig {
    override val id: String = SimpleTimerBossbarConfig.id

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

    override fun write(): JsonObject {
        val json = JsonObject()
        json["title"] = this.title
        json["overlay"] = this.overlay.getName()
        json["colour"] = this.colour.getName()
        return json
    }

    public companion object: TimerBossbarConfigFactory {
        override val id: String = "simple"

        override fun create(data: JsonObject): TimerBossbarConfig {
            val title = data.string("title")
            val overlay = BossBarOverlay.byName(data.stringOrDefault("overlay"))
            val colour = BossBarColor.byName(data.stringOrDefault("colour", data.stringOrDefault("color")))
            return SimpleTimerBossbarConfig(title, overlay, colour)
        }
    }
}