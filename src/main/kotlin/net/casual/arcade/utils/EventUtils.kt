package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerHandler
import net.casual.arcade.events.core.Event

public object EventUtils {
    public fun <T: Event> T.broadcast(): T {
        GlobalEventHandler.broadcast(this)
        return this
    }

    public fun ListenerHandler.registerHandler() {
        GlobalEventHandler.addHandler(this)
    }

    public fun ListenerHandler.unregisterHandler() {
        GlobalEventHandler.removeHandler(this)
    }
}