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
 * class MyPlayerExtension: DataExtension {
 *     var lastSentMessage = ""
 *
 *     override fun getName(): String {
 *         return "arcade_my_player_extension"
 *     }
 *
 *     override fun serialize(): Tag {
 *         return StringTag.valueOf(this.lastSentMessage)
 *     }
 *
 *     override fun deserialize(element: Tag) {
 *         this.lastSentMessage = (element as StringTag).asString
 *     }
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
 * Everything else is handled for you!
 *
 * @see Extension
 */
interface DataExtension: Extension {
    /**
     * This gets the name of your extension.
     *
     * This usually follows the format of `"${modid}_${extension_name}"`,
     * so for example, `"arcade_minigame_extension"`.
     *
     * @return The name of your extension.
     */
    fun getName(): String

    /**
     * This method serializes any data in your extension.
     *
     * The [Tag] that you serialize will be passed into
     * [deserialize] when deserializing.
     *
     * @return The serialized data.
     */
    @OverrideOnly
    fun serialize(): Tag

    /**
     * This method deserializes any data for your extension.
     *
     * The [element] that gets passed in is what you previously
     * serialized with [serialize].
     *
     * @param element The serialized data.
     */
    @OverrideOnly
    fun deserialize(element: Tag)
}