package net.casual.arcade.minigame.lobby

import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.utils.ComponentUtils.crimson
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.minecraft.network.chat.Component
import org.jetbrains.annotations.ApiStatus.OverrideOnly

public interface ReadyChecker {
    @OverrideOnly
    public fun getReadyMessage(ready: HiddenCommand, notReady: HiddenCommand): Component {
        return "Are you ready? ".literal()
            .append("[Yes]".literal().function(ready).lime())
            .append(" ")
            .append("[No]".literal().function(notReady).red())
    }

    @OverrideOnly
    public fun getIsReadyMessage(): Component {
        return " is ready!".literal().lime()
    }

    @OverrideOnly
    public fun getNotReadyMessage(): Component {
        return " is not ready!".literal().red()
    }

    @OverrideOnly
    public fun getAlreadyReadyMessage(): Component {
        return "You are already ready!".literal().crimson()
    }

    @OverrideOnly
    public fun getAlreadyNotReadyMessage(): Component {
        return "You are already marked as not ready!".literal().crimson()
    }

    @OverrideOnly
    public fun broadcast(message: Component)

    @OverrideOnly
    public fun onReady()
}