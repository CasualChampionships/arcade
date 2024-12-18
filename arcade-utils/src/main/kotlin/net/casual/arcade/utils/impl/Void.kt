/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.impl

import org.jetbrains.annotations.ApiStatus.Internal

@Internal
public sealed class Void {
    public companion object: Void()
}