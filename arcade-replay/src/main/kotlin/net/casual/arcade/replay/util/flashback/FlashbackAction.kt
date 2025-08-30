/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util.flashback

import net.minecraft.resources.ResourceLocation

public enum class FlashbackAction(path: String) {
    CacheChunk("action/level_chunk_cached"),
    ConfigurationPacket("action/configuration_packet"),
    CreatePlayer("action/create_local_player"),
    GamePacket("action/game_packet"),
    MoveEntities("action/move_entities"),
    NextTick("action/next_tick"),
    VoiceChat("action/simple_voice_chat_sound_optional");

    public val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath("flashback", path)

    public companion object {
        private val idToAction = FlashbackAction.entries.associateBy { it.id }

        public fun from(id: ResourceLocation): FlashbackAction? {
            return idToAction[id]
        }
    }
}