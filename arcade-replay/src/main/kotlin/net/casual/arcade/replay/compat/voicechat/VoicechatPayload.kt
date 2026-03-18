/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.compat.voicechat

import net.casual.arcade.replay.ArcadeReplay
import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.recorder.packet.RecordablePayload
import net.casual.arcade.replay.util.flashback.FlashbackUtils
import net.casual.arcade.utils.Identifier
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

internal class VoicechatPayload private constructor(
    private val type: CustomPacketPayload.Type<*>,
    private val writer: (FriendlyByteBuf) -> Unit
): CustomPacketPayload, RecordablePayload {
    override fun shouldRecord(recorder: ReplayRecorder): Boolean {
        // We do this check earlier, but might as well do it here for sanity
        return recorder.settings.recordVoiceChat
    }

    override fun record(buf: FriendlyByteBuf) {
        this.writer.invoke(buf)
    }

    override fun type(): CustomPacketPayload.Type<*> {
        return this.type
    }

    companion object {
        /**
         * Mod id of the replay voicechat mod, see [here](https://github.com/henkelmax/replay-voice-chat/blob/master/src/main/java/de/maxhenkel/replayvoicechat/ReplayVoicechat.java).
         */
        private const val REPLAY_VOICECHAT_ID = "replayvoicechat"

        val REPLAY_MOD_LOCATIONAL_TYPE = CustomPacketPayload.Type<VoicechatPayload>(
            Identifier(REPLAY_VOICECHAT_ID, "locational_sound")
        )
        val REPLAY_MOD_ENTITY_TYPE = CustomPacketPayload.Type<VoicechatPayload>(
            Identifier(REPLAY_VOICECHAT_ID, "entity_sound")
        )
        val REPLAY_MOD_STATIC_TYPE = CustomPacketPayload.Type<VoicechatPayload>(
            Identifier(REPLAY_VOICECHAT_ID, "static_sound")
        )

        val FLASHBACK_TYPE = CustomPacketPayload.Type<VoicechatPayload>(
            FlashbackUtils.id("voice_chat_sound")
        )

        val ENCODED_FLASHBACK_TYPE = CustomPacketPayload.Type<VoicechatPayload>(
            ArcadeReplay.id("encoded_flashback_voice_chat_sound")
        )

        fun of(type: CustomPacketPayload.Type<*>, writer: (FriendlyByteBuf) -> Unit): VoicechatPayload {
            return VoicechatPayload(type, writer)
        }
    }
}