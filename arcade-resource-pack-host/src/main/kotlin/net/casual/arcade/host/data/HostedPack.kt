/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host.data

import net.casual.arcade.host.pack.ReadablePack

/**
 * This holds all the data for a [ReadablePack] that
 * is being hosted on a pack host.
 *
 * @param pack The readable pack that is being hosted.
 * @param url The URL that it is being hosted at.
 * @param hash The hash of the [pack].
 */
public data class HostedPack(
    /**
     * The readable pack that is being hosted.
     */
    val pack: ReadablePack,
    /**
     * The URL that it is being hosted at.
     */
    val url: ResolvablePackURL,
    /**
     * The hash of the [pack].
     */
    val hash: String
)
