/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.body

import net.minecraft.server.dialog.body.DialogBody

public abstract class BodyBuilder {
    public abstract fun build(): DialogBody
}