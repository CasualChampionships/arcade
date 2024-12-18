/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions

import net.minecraft.nbt.Tag
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
     * This gets the name of your extension.
     *
     * This usually follows the format of `"${modid}_${extension_name}"`,
     * so for example, `"arcade_minigame_extension"`.
     *
     * @return The name of your extension.
     */
    public fun getName(): String

    /**
     * This method serializes any data in your extension.
     *
     * The [Tag] that you serialize will be passed into
     * [deserialize] when deserializing.
     *
     * @return The serialized data.
     */
    @OverrideOnly
    public fun serialize(): Tag?

    /**
     * This method deserializes any data for your extension.
     *
     * The [element] that gets passed in is what you previously
     * serialized with [serialize].
     *
     * @param element The serialized data.
     */
    @OverrideOnly
    public fun deserialize(element: Tag)
}
