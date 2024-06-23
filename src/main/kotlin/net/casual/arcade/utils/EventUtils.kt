package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerProvider
import net.casual.arcade.events.core.Event

public object EventUtils {
    public fun <T: Event> T.broadcast(): T {
        GlobalEventHandler.broadcast(this)
        return this
    }

    public fun ListenerProvider.registerProvider() {
        GlobalEventHandler.addProvider(this)
    }

    public fun ListenerProvider.unregisterProvider() {
        GlobalEventHandler.removeProvider(this)
    }
}