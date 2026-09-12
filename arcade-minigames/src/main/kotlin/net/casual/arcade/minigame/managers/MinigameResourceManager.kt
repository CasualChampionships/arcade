/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.casual.arcade.minigame.utils.MinigameResources
import net.casual.arcade.pack.PackInfo
import net.minecraft.server.level.ServerPlayer

public class MinigameResourceManager: MinigameResources {
    private val resources = ObjectLinkedOpenHashSet<MinigameResources>()

    public fun add(resources: MinigameResources): Boolean {
        return this.resources.add(resources)
    }

    public fun remove(resources: MinigameResources): Boolean {
        return this.resources.remove(resources)
    }

    override fun getPacks(): Collection<PackInfo> {
        return this.resources.flatMap { it.getPacks() }
    }

    override fun getPacks(player: ServerPlayer): Collection<PackInfo> {
        return this.resources.flatMap { it.getPacks(player) }
    }
}
