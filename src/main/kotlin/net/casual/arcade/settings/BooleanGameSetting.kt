package net.casual.arcade.settings

import net.casual.arcade.utils.json.BooleanSerializer

class BooleanGameSetting(
    name: String,
    value: Boolean,
    options: Map<String, Boolean> = emptyMap()
): GameSetting<Boolean>(name, value, options, BooleanSerializer())