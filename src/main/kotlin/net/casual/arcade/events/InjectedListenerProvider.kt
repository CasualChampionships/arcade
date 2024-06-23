package net.casual.arcade.events

import net.casual.arcade.events.core.Event
import java.util.function.Consumer

public fun interface InjectedListenerProvider {
    public fun injectListenerProviders(event: Event, consumer: Consumer<ListenerProvider>)
}