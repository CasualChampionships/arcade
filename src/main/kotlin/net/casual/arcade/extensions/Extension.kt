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
 * Here's an example of an extension for a [ServerPlayer], which keeps
 * track of the last-sent message from the player.
 * You can then use this data elsewhere in your code.
 * ```kotlin
 * class MyPlayerExtension: Extension {
 *     var lastSentMessage = ""
 *
 *     companion object {
 *         val ServerPlayer.myExtension
 *             get() = this.getExtension(MyPlayerExtension::class.java)
 *
 *         // This must be called in your ModInitializer
 *         fun registerEvents() {
 *             GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
 *                 player.addExtension(MyPlayerExtension())
 *             }
 *             GlobalEventHandler.register<PlayerChatEvent> { (player, message) ->
 *                 player.myExtension.lastSentMessage = message.signedContent()
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @see DataExtension
 * @see ExtensionHolder
 */
public interface Extension