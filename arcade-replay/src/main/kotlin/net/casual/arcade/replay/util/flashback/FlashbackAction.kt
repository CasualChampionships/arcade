/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util.flashback

import net.casual.arcade.replay.ArcadeReplay
import net.minecraft.resources.Identifier

public enum class FlashbackAction(public val id: Identifier) {
    CacheChunk("action/level_chunk_cached"),
    ConfigurationPacket("action/configuration_packet"),
    CreatePlayer("action/create_local_player"),
    GamePacket("action/game_packet"),
    MoveEntities("action/move_entities"),
    NextTick("action/next_tick"),
    VoiceChat("action/simple_voice_chat_sound_optional"),
    EncodedVoiceChat(ArcadeReplay.id("action/encoded_simple_voice_chat_sound_optional"));

    constructor(path: String): this(FlashbackUtils.id(path))

    public fun isOptional(): Boolean {
        return this.id.path.endsWith("optional")
    }

    public companion object {
        private val idToAction = FlashbackAction.entries.associateBy { it.id }

        public fun from(id: Identifier): FlashbackAction? {
            return idToAction[id]
        }
    }
}