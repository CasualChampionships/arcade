/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.scoreboard

import net.minecraft.network.chat.Component
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType

public object DummyScoreboard {
    private val scoreboard = Scoreboard()

    public fun objective(name: String, title: Component = Component.empty(), type: RenderType = RenderType.INTEGER): Objective {
        return Objective(this.scoreboard, name, ObjectiveCriteria.DUMMY, title, type, true, null)
    }

    public fun team(name: String): PlayerTeam {
        return PlayerTeam(this.scoreboard, name)
    }
}