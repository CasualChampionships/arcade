/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.serialization

import kotlinx.coroutines.Job
import net.casual.arcade.minigame.Minigame
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.ApiStatus.OverrideOnly

/**
 * This interface should be used on a minigame implementation
 * to mark it as being serializable.
 *
 * This then provides additional methods to describe how
 * your minigame should be serialized/deserialized.
 *
 * @see Minigame
 */
@OverrideOnly
public interface SerializableMinigame {
    /**
     * The serialization version that the minigame is
     * currently on.
     *
     * When deserializing the [serializationVersion] of the
     * old minigame is passed into [deserialize] to allow
     * you to fix up any data that has changed over versions.
     */
    public val serializationVersion: Int
        get() = 0

    /**
     * The factory that creates instances of `this`
     * minigame.
     *
     * @return The factory that creates instances of this minigame.
     * @see MinigameFactory
     */
    public fun factory(): MinigameFactory

    /**
     * Serializes this minigame.
     *
     * @param output The output to serialize to.
     */
    public fun serialize(output: ValueOutput) {

    }

    /**
     * Deserializes this minigame.
     *
     * @param input The input to deserialize from.
     * @param version The version that the serialized minigame was on.
     */
    public fun deserialize(input: ValueInput, version: Int) {

    }
}

/**
 * This saves the minigame to disk.
 *
 * This runs asynchronously in the background but
 * can be joined via the returned [Job].
 *
 * @param M The minigame instance - is required to be both
 *   an instance of [Minigame] *and* [SerializableMinigame].
 * @return The serialization job, which can be awaited/joined.
 */
public fun <M> M.save(): Job where M: Minigame, M: SerializableMinigame {
    return this.serializer.saveTo(this, this.getSavePath())
}
