package net.casualuhc.arcade.broadcaster

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.server.ServerTickEvent
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit
import net.casualuhc.arcade.utils.PlayerUtils
import net.minecraft.network.chat.Component

@Suppress("unused")
object Broadcaster {
    private val messages = ArrayList<Component>()

    private var lastMessage: Component = Component.empty()
    private var formatter: (Component) -> Component = { it }
    private var interval = 3600

    @JvmStatic
    fun setMessageInterval(time: Int, unit: MinecraftTimeUnit) {
        this.interval = unit.toTicks(time)
    }

    @JvmStatic
    fun setFormatter(formatter: (Component) -> Component) {
        this.formatter = formatter
    }

    @JvmStatic
    fun addMessage(message: Component) {
        this.messages.add(message)
    }

    @JvmStatic
    fun removeMessage(message: Component) {
        this.messages.remove(message)
    }

    init {
        EventHandler.register<ServerTickEvent> {
            if (it.server.tickCount % interval == 0) {
                var message = this.messages.random()
                while (this.messages.size != 1 && message == this.lastMessage) {
                    message = this.messages.random()
                }
                PlayerUtils.forEveryPlayer { player ->
                    player.sendSystemMessage(message)
                }
                this.lastMessage = message
            }
        }
    }
}