/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.minecraft.network.chat.Component
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.criteria.ObjectiveCriteria.DUMMY
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType.INTEGER

public object ScoreboardUtils {
    private val scoreboard = Scoreboard()

    public fun dummyObjective(name: String, title: Component = Component.empty(), type: RenderType = INTEGER): Objective {
        return Objective(this.scoreboard, name, DUMMY, title, type, true, null)
    }

    public fun dummyTeam(name: String): PlayerTeam {
        return PlayerTeam(this.scoreboard, name)
    }
}