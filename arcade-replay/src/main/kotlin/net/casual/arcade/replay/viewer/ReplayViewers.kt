/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.viewer

import net.casual.arcade.replay.io.ReplayFormat
import net.minecraft.server.level.ServerPlayer
import java.nio.file.Path
import java.util.*
import kotlin.io.path.extension

public object ReplayViewers {
    private val viewers = LinkedHashMap<UUID, ReplayViewer>()

    @JvmStatic
    public fun create(path: Path, player: ServerPlayer): ReplayViewer {
        if (this.viewers.containsKey(player.uuid)) {
            throw IllegalArgumentException("Player is already viewing a replay")
        }

        val format = ReplayFormat.formatOf(path)
            ?: throw IllegalStateException("Tried to read unknown replay file type: ${path.extension}")

        val viewer = ReplayViewer(format.reader(path), player.connection)
        this.viewers[player.uuid] = viewer
        return viewer
    }

    @JvmStatic
    public fun remove(uuid: UUID): ReplayViewer? {
        return this.viewers.remove(uuid)
    }

    @JvmStatic
    public fun has(uuid: UUID): Boolean {
        return this.viewers.containsKey(uuid)
    }

    @JvmStatic
    public fun viewers(): Collection<ReplayViewer> {
        return Collections.unmodifiableCollection(this.viewers.values)
    }
}