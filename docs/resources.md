# Resources

Arcade provides loads of utilities for resource packs, from creating them, to hosting them and managing them.

## Built-In Pack Providers

Arcade ships with some of the commonly used resource packs as well as some special resource packs that are required for some features in Arcade. Some of these include:
- Spaces Font: Used for negative spacing
- Padding Font: Used for no-split padding and spacing
- No Text Shadow: Used for removing text shadows
- Player Heads: Used for rending player heads as text
- Hide Player List Heads: Used for hiding player heads in the tab list
- Hide Player List Ping: Used for hiding player's ping in the tab list
- Mini Minecraft Font: A small-caps version of the Minecraft font
- Action Bar Font: Provides the default Minecraft font but shifted downwards for rendering on multiple lines for the action bar
- Mini Action Bar Font: Same as above but for the Mini Minecraft font

All of these packs can be found in the `ArcadePacks` object and are all an instance of `NamedResourcePackCreator`. They provide you with the resources to build these packs, you can then write them to disk using the `buildTo` method:

```kotlin
val directory = FabricLoader.getInstance().gameDir.resolve("resource-packs")
directory.createDirectories()
ArcadePacks.PLAYER_HEADS_PACK.buildTo(directory)
```

Once you have all your packs written to disk, you can host them to players joining your server, this will be further discussed in the [Pack Hosting Section](#pack-hosting).

### Spaces Font

If you are using the space font, Arcade provides some utilities for creating space components `ComponentUtils.space` will create a spaced component with the specified space:
```kotlin
val spaced: Component = ComponentUtils.space(10)
```

### Padding Font

Padding is similar to the space font but provides some more utilities in the case of `PaddingNoSplitFontResources` which allows you to have negative space with no width, allowing you to achieve some things that you cannot achieve with negative spacing at a slight performance cost.

You can request padding from `-255` to `255`

```kotlin
val splitPadding: Component = PaddingSplitFontResources.padding(10)
val noSplitPadding: Component = PaddingNoSplitFontResources.padding(10)
```

### Player Heads

To create player heads with components, we essentially just re-build the texture with coloured pixels.

If you have this installed you can use the `PlayerHeadComponents` object to create player head components. You can do this by calling `PlayerHeadComponents#getHead`, since it requires fetching the player's skin, this runs asynchronously and returns a `CompletableFuture<Component>`, but you can instead call `PlayerHeadComponents#getHeadOrDefault` which will return a steve head if the player's head isn't available yet.

```kotlin
// We can fetch using a reference to our player
val player: ServerPlayer = // ...
val headFuture: CompletableFuture<Component> = PlayerHeadComponents.getHead(player)
val headOrDefault: Component = PlayerHeadComponents.getHeadOrDefault(player)

// We can also simply fetch by username
val username: String = "senseiwells"
val headFuture: CompletableFuture<Component> = PlayerHeadComponents.getHead(username)
val headOrDefault: Component = PlayerHeadComponents.getHeadOrDefault(username)
```

## Custom Pack Providers

If you want to create your own resource pack, you can create an instance of `NamedResourcePackCreator`. This holds an instance of a `ResourcePackCreator` which is part of [polymer's resource pack api](https://polymer.pb4.eu/0.5.x/polymer-resource-pack/basics/). This is where you can add all your assets to the resource pack.

We can build a creator with the `NamedResourcePackCreator#named` method, this then takes a lambda which provides the `ResourcePackCreator` instance:

```kotlin
NamedResourcePackCreator.named("my-resource-pack") {
    val creator: ResourcePackCreator = this

    // These are optional:
    packIcon = /* Byte array of image */
    packDescription = Component.literal("Example pack description")
}
```

Using the `ResourcePackCreator#addAssetSource` method from polymer, we can add assets that we've specified in our `./src/resources/assets` folder by passing in our mod namespace:
```kotlin
NamedResourcePackCreator.named("my-resource-pack") {
    addAssetSource("namespace")
}
```

Arcade provides a few more utility methods for adding resources to packs:
```kotlin
NamedResourcePackCreator.named("my-resource-pack") {
    // Adds the lang files from the specified path under the specified namespace
    addLangsFrom(
        namespace = "minecraft",
        path = FabricLoader.getInstance().getModContainer("namespace").get()
            .findPath("assets/minecraft/lang").get()
    )
    
    // Adds the lang files from your ./src/resources/data/lang directory
    // This may be useful if you're using server-translations and
    // you are already storing lang files here
    addLangsFromData("namespace")
}
```

There are a few more but these will be discussed in the sections below:

### Font Resources

Creating and managing bitmap fonts for resource packs is a nightmare, especially when you're constantly adding new things into the files and need to keep track of what unicode character maps to what. Arcade provides a clean solution.

You can create an object that inherits from `FontResources`, you can then statically register all your textures in this object which you can then reference to get instances of those `Components`. Add these resources to your `ResourcePackCreator` and the rest is handled for you!

First we start by extending the `FontResources` class, this takes in a `ResourceLocation` this will be the identifier used for your font:
```kotlin
object MyFontResources: FontResources(ResourceLocation("modid", "my_font")) {
    
}
```

We can now declare what textures we want using the `bitmap` method. We must provide the location of the texture, this is a `ResourceLocation`. If the texture is declared under your `modid` namespace in the font path you can use the `at` method which will automatically create a `Resourcelocation` under your namespace starting in the font directory. We can also specify the ascent and height of the texture here.
```kotlin
object MyFontResources: FontResources(ResourceLocation("modid", "my_font")) {
    val MY_CUSTOM_TEXTURE by bitmap(at("location_of_custom_texture.png"))
    val MY_CUSTOM_TEXTURE_2 by bitmap(at("location_of_custom_texture_2.png"), ascent = 5, height = 20)
}
```

You can add as many textures as you want. Then when generating your resource pack you can simply add `ResourcePackCreator#addFont` and pass in your font resources object:
```kotlin
NamedResourcePackCreator.named("my-resource-pack") {
    // ...
    addFont(MyFontResources)
}
```

And just like that, all the json for the font will be automatically generated for you.

Now, to create the text components in-game, we can simply just reference the fields where we declared the textures, they will already have the correct unicode character and font applied:
```kotlin
val component: Component = MyFontResources.MY_CUSTOM_TEXTURE
```

### Sound Resources

The way sound resources work is very similar to that of fonts. We can create an object that inherits from `SoundResources` and then static register all your sounds in this object:

```kotlin
object MySoundResources: SoundResources("namespace") {
    val MY_CUSTOM_SOUND = sound(
        at("location_of_custom_sound"),
        // These are all optional
        volume = 1.0F,
        pitch = 1.0F,
        stream = false,
        attenuationDistance = 16,
        isStatic = false,
        preload = false,
    )
    val MY_CUSTOM_SOUND_EVENT = event(
        at("location_of_custom_sound_event"),
        // These are all optional
        volume = 1.0F,
        pitch = 1.0F,
        stream = false,
        attenuationDistance = 16,
        isStatic = false,
        preload = false,
    )
    val MY_CUSTOM_SOUND_GROUP = group(
        "sound_group_id",
        // These are optional
        attenuationDistance = 16,
        isStatic = false,
    ) {
        // Inside here we define more sounds/events
        sound(at("location_of_custom_sound"))
    }
}
```

The difference between `sound` and `event` is that `sound` causes the value of the resource location to be interpreted as the name of a file, while `event` causes the value of the resource location to be interpreted as the name of an already defined `event`.

Group's allow you to group multiple sounds into a singular sound event. When the sound event is then played, it will pick one of the random sounds defined in the group.

You can add as many sounds as you want. Then when generating your resource pack you can simply add `ResourcePackCreator#addSounds` and pass in your sound resources object:
```kotlin
NamedResourcePackCreator.named("my-resource-pack") {
    // ...
    addSounds(MySoundResources)
}
```

And just like that, all the json for the sounds will be automatically generated for you.

Similarly to the fonts, we can just reference the fields where we defined the sounds:
```kotlin
val sound: SoundEvent = MySoundResources.MY_CUSTOM_SOUND
```

## Pack Hosting

It's great that we can dynamically generate and build our resource packs, but we also need a way to host our packs so our players can download the packs.

We can do this by creating an instance of `PackHost`, we then need to specify what packs we want to host. We do this by giving the `PackHost` a `PackSupplier`. One of the implementations of this is the `DirectoryPackSupplier`:
```kotlin
val host = PackHost()

val resourcesPath = Path.of(/* Path with all your resource packs */)
host.addPacks(DirectoryPackSupplier(resourcesPath))
```

Alternatively, you can create your own implementation of `PackSupplier` returning a collection of `ReadablePack`.

Once you'd added all your resource packs we can start the pack host, to do this, you must specify the server-ip and the port that the packs will be hosting on:
```kotlin
val host = PackHost()

host.start(
    hostIp = "0.0.0.0",
    // Optional, default port is 24464
    hostPort = 24464,
    // Whether the URLs of the packs should be randomized
    randomise = false
)
```

Once you have started hosting packs we can get the information to send this to players, we can call `getHostedPack` to retrieve the info about a named pack. This may be null if the pack requested doesn't exist:
```kotlin
// Get the hosted pack by its name
val pack: HostedPack? = host.getHostedPack("my-resource-pack")
```

Once we have a reference to a `HostedPack` we can convert it into `PackInfo` which can then be sent to players:
```kotlin
val pack: HostedPack = // ...

val info = pack.toPackInfo(
    // Optional
    required = false,
    prompt = Component.literal("Server sided resource pack")
)

val player: ServerPlayer = // ...
player.sendResourcePack(info)
```


## Data Generation

Arcade also provides some data-generation utilities. Currently, this is aimed at generating entries into lang files.

> TODO