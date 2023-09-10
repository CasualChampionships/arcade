package net.casual.arcade.utils.impl

import org.jetbrains.annotations.ApiStatus.Internal

@Internal
sealed class Void {
    internal companion object: Void()
}