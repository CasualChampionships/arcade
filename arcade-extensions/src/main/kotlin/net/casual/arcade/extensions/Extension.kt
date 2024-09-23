package net.casual.arcade.extensions

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

/**
 * This interface represents any [Extension] that
 * can be added to an [ExtensionHolder].
 *
 * By default, the only [ExtensionHolder]s are
 * [ServerPlayer], [ServerLevel], and [PlayerTeam].
 *
 * Extensions can be used to add custom data to
 * certain Minecraft classes, furthermore this
 * data can be serialized and deserialize with the
 * use of a [DataExtension].
 *
 * Here's an example of an extension for a [ServerLevel], which keeps
 * track of the last modified block position in the world.
 * You can then use this data elsewhere in your code.
 * ```kotlin
 * class MyLevelExtension: Extension {
 *     var lastModifiedBlockPos: BlockPos? = null
 *
 *     companion object {
 *         val ServerLevel.myExtension
 *             get() = this.getExtension(MyLevelExtension::class.java)
 *
 *         // This must be called in your ModInitializer
 *         fun registerEvents() {
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
 * For Player related extensions please use [PlayerExtension].
 *
 * @see DataExtension
 * @see ExtensionHolder
 */
public interface Extension