/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.minecraft.core.Holder
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.Attribute

public data class PlayerAttributeUpdatedEvent(
    override val player: ServerPlayer,
    val attribute: Holder<Attribute>
): PlayerEvent