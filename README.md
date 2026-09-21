# <img src="./src/main/resources/assets/icon.png" align="center" width="64px"/> Arcade


<!-- #region about -->
## About the Project

Arcade is a server-side Minecraft api made in Kotlin, providing
a wide array of functionality primarily aimed at server-sided
minigame development.

Arcade is built up of many different modules, a few notable ones
include, supporting custom runtime dimensions, replay recording/playback,
a virtual (packet based) entity system, and a minigame api. A list of
all the modules available can be found in the [documentation](https://arcade.casualchampionships.net).

## Adding to your Project

It's recommended that you manage your arcade dependency via the
[Joystick](https://github.com/CasualChampionships/joystick) Gradle plugin.

If you are developing minigames using arcade, you will want all the 
server-side modules, you can do this by adding the following to your
`build.gradle.kts`.

```kts
plugins {
    id("net.casualchampionships.joystick") version "1.0.1"
}

arcade {
    version = "0.14.0-beta.5+26.3"
    modules("arcade")
}
```

If you do not want to bundle the whole of arcade, declare only the modules you
need. Each module's page in the [documentation](https://arcade.casualchampionships.net) 
shows its snippet.

Every module is also published as a plain maven artifact under
`net.casualchampionships` on `https://maven.casualchampionships.net/snapshots`
if you would rather manage the dependencies yourself.
<!-- #endregion about -->

## Documentation

The api's documentation can be found [here](https://arcade.casualchampionships.net).

The documentation details how to get started with each of Arcade's modules and what features they provide.

## License

Distributed under the MIT License. See [MIT License](https://opensource.org/licenses/MIT) for more information.
