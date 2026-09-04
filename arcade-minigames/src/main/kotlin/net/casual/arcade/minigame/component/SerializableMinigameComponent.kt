/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.component

import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.Identifier
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

/**
 * A [MinigameComponent] that also provides the ability to
 * be serialized and deserialized.
 *
 * In addition to the required [serialize] and [deserialize]
 * methods that need to be implemented, you must also register
 * a [MinigameComponentFactory] to [MinigameRegistries.MINIGAME_COMPONENT_FACTORY]
 * under the same id as your [MinigameComponentType.id] which
 * allows the component to be reconstructed.
 *
 * An example of how to implement this interface can be seen below:
 * ```
 * class MySerializableComponent: SerializableMinigameComponent {
 *     override fun serialize(output: ValueOutput) {
 *         // ...
 *     }
 *
 *     override fun deserialize(input: ValueInput, version: Int) {
 *         // ...
 *     }
 *
 *     override fun type(): MinigameComponentType<*> {
 *         return TYPE
 *     }
 *
 *     companion object {
 *         val TYPE = MinigameComponentType<MySerializableComponent>(Identifier("namespace", "path"))
 *
 *         // Call this from your ModInitializer
 *         internal fun register() {
 *             MinigameComponentFactory.register(TYPE) { minigame ->
 *                 MySerializableComponent()
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @see MinigameComponent
 */
public interface SerializableMinigameComponent: MinigameComponent {
    /**
     * The serialization version that the component is
     * currently on.
     *
     * When deserializing the [serializationVersion] of the
     * old component is passed into [deserialize] to allow
     * you to fix up any data that has changed over versions.
     */
    public val serializationVersion: Int
        get() = 0

    /**
     * Serializes this component.
     *
     * @param output The output to serialize to.
     */
    public fun serialize(output: ValueOutput)

    /**
     * Deserializes this component.
     *
     * @param input The input to deserialize from.
     * @param version The version that the serialized component was on.
     */
    public fun deserialize(input: ValueInput, version: Int)
}