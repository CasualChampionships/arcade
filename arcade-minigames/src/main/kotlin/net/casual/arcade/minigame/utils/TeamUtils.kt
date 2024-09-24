package net.casual.arcade.minigame.utils

import net.casual.arcade.minigame.ducks.OverridableColor
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.scores.PlayerTeam

public fun PlayerTeam.setHexColor(color: Int?) {
    (this as OverridableColor).`arcade$setColor`(color)
}

public fun PlayerTeam.getHexColor(): Int? {
    return (this as OverridableColor).`arcade$getColor`() ?: this.color.color
}

public fun MutableComponent.color(team: PlayerTeam): MutableComponent {
    val color = team.getHexColor()
    if (color != null) {
        this.withColor(color)
    }
    return this
}