package net.casual.arcade.config

import net.casual.arcade.Arcade
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerSaveEvent

public object ArcadeConfig: CustomisableConfig(Arcade.path.resolve("config.json")){
    internal fun registerEvents() {
        this.read()
        GlobalEventHandler.register<ServerSaveEvent> {
            this.write()
        }
    }
}