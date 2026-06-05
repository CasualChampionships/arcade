/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.action

import net.minecraft.server.dialog.action.Action

public abstract class ActionBuilder {
    public abstract fun build(): Action
}