/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.serialization

import net.casual.arcade.minigame.Minigame
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.ApiStatus.OverrideOnly

@OverrideOnly
public interface SerializableMinigame {
    public val serializationVersion: Int
        get() = 0

    public fun factory(): MinigameFactory

    public fun serialize(output: ValueOutput) {

    }

    public fun deserialize(input: ValueInput, version: Int) {

    }
}

public fun <M> M.save() where M: Minigame, M: SerializableMinigame {
    this.serializer.saveTo(this, this.getSavePath())
}
