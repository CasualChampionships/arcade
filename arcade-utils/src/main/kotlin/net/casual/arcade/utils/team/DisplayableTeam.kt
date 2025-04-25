package net.casual.arcade.utils.team

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.TeamUtils.getHexColor
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.world.scores.PlayerTeam
import java.util.*
import kotlin.jvm.optionals.getOrNull

public data class DisplayableTeam(val name: Component, val color: Int?) {
    public companion object {
        public val CODEC: Codec<DisplayableTeam> = RecordCodecBuilder.create { instance ->
            instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(DisplayableTeam::name),
                Codec.INT.optionalFieldOf("color").forGetter { team -> Optional.ofNullable(team.color) }
            ).apply(instance) { name, color -> DisplayableTeam(name, color.getOrNull()) }
        }

        public fun PlayerTeam.displayable(): DisplayableTeam {
            return DisplayableTeam(this.displayName, this.getHexColor())
        }
    }
}