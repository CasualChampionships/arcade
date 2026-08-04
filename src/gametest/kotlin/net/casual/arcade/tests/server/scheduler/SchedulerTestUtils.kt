/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.scheduler

import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.TagValueInput
import net.minecraft.world.level.storage.TagValueOutput

fun SimpleTickedScheduler.tick(times: Int) {
    repeat(times) { this.tick() }
}

fun SimpleTickedScheduler.save(server: MinecraftServer): CompoundTag {
    return ArcadeUtils.createProblemReporter().use { reporter ->
        val output = TagValueOutput.createWithContext(reporter, server.registryAccess())
        this.serialize(output.childrenList("tasks"))
        output.buildResult()
    }
}

fun SimpleTickedScheduler.load(server: MinecraftServer, tag: CompoundTag, owner: Any?) {
    ArcadeUtils.createProblemReporter().use { reporter ->
        val input = TagValueInput.create(reporter, server.registryAccess(), tag)
        this.deserialize(input.childrenListOrEmpty("tasks"), owner)
    }
}
