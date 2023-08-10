package net.casualuhc.arcade.utils

import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

object CommandSourceUtils {
    fun CommandSourceStack.success(component: Component, log: Boolean = false) {
        this.sendSuccess({ component }, log)
    }

    fun CommandSourceStack.fail(component: Component) {
        this.sendFailure(component)
    }
}