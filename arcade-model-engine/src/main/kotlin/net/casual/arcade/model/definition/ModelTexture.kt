/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.definition

import java.awt.image.BufferedImage

public class ModelTexture(
    public val name: String,
    public val image: BufferedImage,
    public val width: Int,
    public val height: Int,
    public val frameTime: Int = 1
) {
    public val animated: Boolean
        get() = this.width > 0 && this.height > this.width && this.height % this.width == 0
}