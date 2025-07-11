/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.pack

import net.casual.arcade.host.data.ResolvablePackURL
import net.minecraft.network.chat.Component
import java.util.*

/**
 * This class contains all the information required by the
 * client to download a server-side resource pack.
 *
 * @param url The URL to download the pack from.
 * @param hash The hash of the resource pack.
 * @param required Whether the resource pack is required to play on the server.
 * @param prompt The prompt given to the player about this resource pack; may be null.
 */
public data class PackInfo(
    /**
     * The URL to download the pack from.
     */
    val url: ResolvablePackURL,
    /**
     * The hash of the resource pack.
     */
    val hash: String,
    /**
     * Whether the resource pack is required to play on the server.
     */
    val required: Boolean,
    /**
     * The prompt given to the player about this resource pack; may be null.
     */
    val prompt: Component?,
    /**
     * The uuid identifying this pack.
     */
    val uuid: UUID = UUID.nameUUIDFromBytes(url.resolve().encodeToByteArray())
)