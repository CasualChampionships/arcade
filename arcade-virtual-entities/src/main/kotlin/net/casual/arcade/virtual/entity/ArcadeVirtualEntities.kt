/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity

import net.casual.arcade.virtual.entity.extensions.EntityAttachmentExtension
import net.casual.arcade.virtual.entity.extensions.LevelAttachmentExtension
import net.casual.arcade.virtual.entity.extensions.PlayerAttachmentObserverExtension
import net.fabricmc.api.ModInitializer

public object ArcadeVirtualEntities: ModInitializer {
    public const val MOD_ID: String = "arcade-virtual-entities"

    override fun onInitialize() {
        EntityAttachmentExtension.registerEvents()
        LevelAttachmentExtension.registerEvents()
        PlayerAttachmentObserverExtension.registerEvents()
    }
}