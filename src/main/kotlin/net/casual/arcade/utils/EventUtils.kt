package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerHandler

object EventUtils {
    fun ListenerHandler.registerHandler() {
        GlobalEventHandler.addHandler(this)
    }

    fun ListenerHandler.unregisterHandler() {
        GlobalEventHandler.removeHandler(this)
    }
}