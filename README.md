# Arcade

Arcade is a server-side Minecraft API written in Kotlin that
provides many utilities to make server side modding much easier.

## Events

Arcade provides a simple way to listen to core game events as
well as providing a way to create and broadcast your own events.

Events may be cancelled or invoked allowing for flexibility within
your listeners. You are able to give your listener a priority to
determine in which order listeners should be fired.

All events are data classes allowing you to easily access the
event data by unwrapping them.

Events should be registered in your mod initializer to ensure
that they registered before an event is fired.

Here are some examples:

```kotlin
// Called whenever a player is ticked
EventHandler.register<PlayerTickEvent> { (player) ->
    val message = // ...
    player.sendSystemMessage(message)
}
// Called whenever a player places a block
EventHandler.register<PlayerBlockPlacedEvent> { event ->
    val (player, item, state, context) = event
    if (item == Items.GRASS_BLOCK) {
        event.cancel()
    }
}
// Called whenever recipes are reloaded
EventHandler.register<ServerRecipeReloadEvent> { event ->
    val myRecipe = // ...
    event.add(myRecipe)
}
```

## Extensions

Another core part of this API are extensions, these allow you
to add your own data to existing classes. This data can then be
serialized and deserialize with the existing class allowing for
very easy integration.

Here's an example:

```kotlin
// Add the extension when the class is instantiated
EventHandler.register<PlayerCreatedEvent> { (player) ->
    // Extension method: PlayerUtils.addExtension
    player.addExtension(MyExtension())
}

Eventhandler.register<PlayerTickEvent> { (player) -> 
    // Access the extension using the extension getter
    player.myExtension.ticks++
}

class MyExtension: Extension {
    // And extension data
    var ticks = 0
    
    companion object {
        // Much more readable if you use an extension getter
        val ServerPlayer.myExtension
            get() = this.getExtension(MyExtension::class.java)
    }
}
```

To create an extension that is also serializable you just implement
`DataExtension` instead of `Extension`. These will automatically be
serialized and deserialized when the rest of the object does.

```kotlin
class MyExtension: DataExtension {
    var ticks = 0

    override fun getName(): String {
        return "MyExtension"
    }

    override fun serialize(): Tag {
        val compound = CompoundTag()
        compound.putInt("ticks", this.ticks)
        return compound
    }

    override fun deserialize(element: Tag) {
        val compound = element as CompoundTag
        this.ticks = compound.getInt("ticks")
    }
}
```

## Miscellaneous

Some other minor features that you may find useful.

### Task Scheduling

Arcade provides an implementation of a scheduler that allows
you to schedule lambdas with a given amount of delay.

```kotlin
Scheduler.schedule(5, MinecraftTimeUnit.Ticks) {
    println("Ran after 5 ticks")
}
Scheduler.schedule(10, MinecraftTimeUnit.Seconds) {
    println("Ran after 10 seconds")
}
Scheduler.schedule(1, MinecraftTimeUnit.MinecraftDay) {
    println("Ran after 1 Minecraft day")
}
Scheduler.schedule(3, MinecraftTimeUnit.Hours) {
    println("Ran after 3 hours")
}
```

### Resource Pack Hosting

Arcade provides a way for you to host your resource packs
directly on your server.

```kotlin
lateinit var host: ResourcePackHost

EventHandler.register<ServerLoadedEvent> {
    val packDirectory = Path.of(/* ... */)
    host = LocalResourcePackHost(packDirectory)
    // Use actual server IP
    val serverIp = "127.0.0.1"
    val port = 24464
    // Whether hosted pack url is random or just pack name
    val randomUrl = false
    host.start(serverIp, port, randomUrl)
}

EventHandler.register<PlayerJoinEvent> { (player) ->
    // Resource pack name in your pack directory
    val pack = host.getHostedPack("MyResourcePack")
    if (pack != null) {
        val url = pack.url
        val hash = pack.hash
        val required = false
        val message = // ...
        player.sendTexturePack(url, hash, required, message)
    }
}
```

### World Border

Arcade's own world border classes allowing more flexibility
since the original world border classes are all private.

Arcade's moving border implementation also relies on in-game
ticks compared to real-time which vanilla uses.

### Commands

Arcade implements an `EnumArgument` allowing you to create
an argument where only enums values are can be selected with
no other command type registration like the vanilla ones.

### Advancements

Arcade provides a way to add your own advancements at runtime
by using the `ServerAdvancementReloadEvent`.

You can create your own advancements by utilising the 
`AdvancementBuilder` also provided by Arcade.

### Recipes

Much like advancements arcade provides a way to add your own
recipes at runtime by using `ServerRecipeReloadEvent`.

You can create your own recipes by utilising the
`CraftingRecipeBuilder` or extending the `ArcadeCustomRecipe` class.

### Utilities

Arcade also provides many other utilities which mainly add
extension methods to exising classes, for example:

```kotlin
EventHandler.regster<PlayerTickEvent> { (player) ->
    // All utils here are from PlayerUtils
    val message = // ...
    // Makes the player send a message
    player.message(message)
    // Sends a title to the player
    player.sendTitle(message)
    val advancement = // ...
    // Grants the complete advancement
    player.grantAdvancement(advancement)
}
```