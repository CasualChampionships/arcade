package net.casual.arcade.settings

import net.casual.arcade.utils.json.IntSerializer

public class IntegerGameSetting(
    name: String,
    value: Int,
    options: Map<String, Int> = emptyMap()
): GameSetting<Int>(name, value, options, IntSerializer)