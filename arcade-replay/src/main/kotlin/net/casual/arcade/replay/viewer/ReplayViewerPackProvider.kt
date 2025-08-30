/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.viewer

import net.casual.arcade.host.GlobalPackHost
import net.casual.arcade.host.pack.ReadablePack
import net.casual.arcade.host.pack.provider.PackProvider
import net.casual.arcade.utils.network.ResolvableURL

public class ReplayViewerPackProvider(
    private val viewer: ReplayViewer
): PackProvider {
    private val regex = Regex("""^replay/([0-9a-f]{5,40})$""")

    override fun get(name: String): ReadablePack? {
        val match = this.regex.find(name) ?: return null
        val hash = match.groups[1]!!.value
        val stream = this.viewer.getResourcePack(hash) ?: return null
        return ReadablePack.of(hash, stream)
    }

    public fun url(hash: String): String {
        return GlobalPackHost.createUrl("replay/$hash").resolve(this.viewer.connection)
    }
}