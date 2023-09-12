package net.casual.arcade.settings

import net.casual.arcade.utils.json.DoubleSerializer

public class DoubleGameSetting(
    name: String,
    value: Double,
    options: Map<String, Double> = emptyMap()
): GameSetting<Double>(name, value, options, DoubleSerializer())