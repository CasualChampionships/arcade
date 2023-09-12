package net.casual.arcade.settings

import net.casual.arcade.utils.json.LongSerializer

public class LongGameSetting(
    name: String,
    value: Long,
    options: Map<String, Long> = emptyMap()
): GameSetting<Long>(name, value, options, LongSerializer())