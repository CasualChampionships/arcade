/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.ApiStatus.OverrideOnly

/**
 * This interface is an extension of [Extension] that allows
 * you to further serialize any data in your [Extension].
 *
 * Building off the previous example in [Extension] to now
 * make it a DataExtension:
 * ```kotlin
 * public class MyLevelExtension: DataExtension {
 *     public var lastModifiedBlockPos: BlockPos? = null
 *
 *     override fun getName(): String {
 *         return "arcade_my_level_extension"
 *     }
 *
 *     override fun serialize(): Tag? {
 *         val pos = this.lastModifiedBlockPos ?: return null
 *         return NbtUtils.writeBlockPos(pos)
 *     }
 *
 *     override fun deserialize(element: Tag) {
 *         this.lastModifiedBlockPos = NbtUtils.readBlockPos(element as CompoundTag)
 *     }
 *
 *     public companion object {
 *         public val ServerLevel.myExtension: MyLevelExtension
 *             get() = this.getExtension(MyLevelExtension::class.java)
 *
 *         // This must be called in your ModInitializer
 *         public fun registerEvents() {
 *             GlobalEventHandler.register<LevelExtensionEvent> { event ->
 *                 event.addExtension(MyLevelExtension())
 *             }
 *             GlobalEventHandler.register<LevelBlockChangedEvent> { (level, pos, _, _) ->
 *                 level.myExtension.lastModifiedBlockPos = pos
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * Everything else is handled for you!
 *
 * @see Extension
 */
public interface DataExtension: Extension {
    /**
     * This gets the id of your extension.
     *
     * @return The id of your extension.
     */
    public fun getId(): ResourceLocation

    /**
     * This method serializes any data in your extension.
     *
     * @param output The output to store your data to.
     */
    @OverrideOnly
    public fun serialize(output: ValueOutput)

    /**
     * This method deserializes any data for your extension.
     *
     * @param input The input data.
     */
    @OverrideOnly
    public fun deserialize(input: ValueInput)
}
