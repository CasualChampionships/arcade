/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.input

import net.minecraft.server.dialog.input.InputControl

public abstract class InputBuilder {
    public abstract fun build(): InputControl
}