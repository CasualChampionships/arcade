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
        return Objective(this.scoreboard, name, DUMMY, title, type)
    }

    public fun dummyTeam(name: String): PlayerTeam {
        return PlayerTeam(this.scoreboard, name)
    }
}