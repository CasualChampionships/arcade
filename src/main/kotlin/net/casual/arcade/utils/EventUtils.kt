package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerHandler
import net.casual.arcade.events.core.Event

object EventUtils {
    fun <T: Event> T.broadcast(): T {
        GlobalEventHandler.broadcast(this)
        return this
    }

    fun ListenerHandler.registerHandler() {
        GlobalEventHandler.addHandler(this)
    }

    fun ListenerHandler.unregisterHandler() {
        GlobalEventHandler.removeHandler(this)
    }
}