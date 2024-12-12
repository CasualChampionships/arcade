package net.casual.arcade.events

import net.casual.arcade.events.common.Event
import java.util.function.Consumer

/**
 * This allows us to inject [ListenerProvider]s depending on the context
 * of a specific event.
 *
 * @see GlobalEventHandler.addInjectedProvider
 */
public fun interface InjectedListenerProvider {
    /**
     * This method is called whenever the given [event] is fired.
     *
     * We can then determine if we want to add any additional [ListenerProvider]s
     * and pass them into the [consumer].
     *
     * @param event The event being fire.
     * @param consumer The consumer to add any additional [ListenerProvider]s.
     */
    public fun injectListenerProviders(event: Event, consumer: Consumer<ListenerProvider>)
}