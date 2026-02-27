/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam

/**
 * This interface represents any [Extension] that
 * can be added to an [ExtensionHolder].
 *
 * By default, the only [ExtensionHolder]s are
 * [ServerPlayer], [Entity], [ServerLevel], and [PlayerTeam].
 *
 * Extensions can be used to add custom data to
 * certain Minecraft classes, furthermore this
 * data can be serialized and deserialize with the
 * use of a [SerializableExtension].
 *
 * Here's an example of an extension for a [ServerLevel], which keeps
 * track of the last modified block position in the world.
 * You can then use this data elsewhere in your code.
 * ```kotlin
 * class MyLevelExtension: Extension {
 *     var lastModifiedBlockPos: BlockPos? = null
 *
 *     companion object {
 *         val ServerLevel.myExtension: MyLevelExtension
 *             get() = this.getExtension()
 *
 *         // This must be called in your ModInitializer
 *         internal fun registerEvents() {
 *             GlobalEventHandler.Server.register<LevelExtensionEvent> { event ->
 *                 event.addExtension(MyLevelExtension())
 *             }
 *             GlobalEventHandler.Server.register<LevelBlockChangedEvent> { (level, pos, _, _) ->
 *                 level.myExtension.lastModifiedBlockPos = pos
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * For Player related extensions please use [PlayerExtension].
 *
 * @see SerializableExtension
 * @see ExtensionHolder
 */
public interface Extension