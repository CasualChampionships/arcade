package net.casual.arcade.config

import net.casual.arcade.Arcade

public object ArcadeConfig: CustomisableConfig(Arcade.path.resolve("config.json"))