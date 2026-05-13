/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.ducks;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRuleMap;

public interface GameRulesData {
    FeatureFlagSet arcade_getFeatureFlagSet();
    GameRuleMap arcade_getGameRuleMap();
}
