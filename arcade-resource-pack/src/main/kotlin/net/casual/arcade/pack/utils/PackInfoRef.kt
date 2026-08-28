/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.utils

import net.casual.arcade.pack.PackInfo
import net.casual.arcade.pack.host.HostedPack
import net.casual.arcade.pack.host.HostedPackRef
import net.casual.arcade.pack.utils.ResourcePackUtils.toPackInfo
import net.minecraft.network.chat.Component

public class PackInfoRef internal constructor(
    private val ref: HostedPackRef,
    private val required: Boolean,
    private val prompt: Component?
) {
    public fun isHosted(): Boolean {
        return this.ref.isHosted()
    }

    public fun getNow(): PackInfo? {
        return this.ref.getNow()?.toInfo()
    }

    public suspend fun await(): PackInfo {
        return this.ref.await().toInfo()
    }

    public fun join(): PackInfo {
        return this.ref.join().toInfo()
    }

    private fun HostedPack.toInfo(): PackInfo {
        return this.toPackInfo(required, prompt)
    }
}