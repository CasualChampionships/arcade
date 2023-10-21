package net.casual.arcade.gui.ready

import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.utils.ComponentUtils.crimson
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.literal
import net.minecraft.network.chat.Component
import org.jetbrains.annotations.ApiStatus.OverrideOnly

public interface Ready {
    @OverrideOnly
    public fun getReadyMessage(ready: HiddenCommand, notReady: HiddenCommand): Component {
        return "Are you ready? ".literal()
            .append("[Yes]".literal().function(ready).green())
            .append(" ")
            .append("[No]".literal().function(notReady).crimson())
    }

    @OverrideOnly
    public fun getIsReadyMessage(): Component {
        return " is ready!".literal()
    }

    @OverrideOnly
    public fun getNotReadyMessage(): Component {
        return " is not ready!".literal()
    }

    @OverrideOnly
    public fun broadcast(message: Component)

    @OverrideOnly
    public fun onReady()
}