/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.minigame

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.serialization.SerializableMinigame
import net.casual.arcade.minigame.serialization.save
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import java.util.UUID
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteRecursively

public fun <M: Minigame> TestContext.minigame(
    constructor: (MinecraftServer, UUID) -> M
): TestMinigameBuilder<M> {
    return TestMinigameBuilder(this, constructor)
}

@OptIn(ExperimentalPathApi::class)
public suspend fun <M> TestContext.reload(minigame: M): M where M: Minigame, M: SerializableMinigame {
    val path = minigame.getSavePath()
    minigame.save().join()

    val copy = this.server.getWorldPath(LevelResource.ROOT)
        .resolve("gametest-minigames")
        .resolve(UUID.randomUUID().toString())
    copy.createParentDirectories()
    path.copyToRecursively(copy, followLinks = false)

    minigame.close()

    val restored = try {
        Minigames.read(copy, this.server)
    } finally {
        copy.deleteRecursively()
    }

    this.track(AutoCloseable(restored::close))

    val type = minigame.javaClass
    this.assertTrue(type.isInstance(restored), "Restored minigame was not a ${type.simpleName}")
    return type.cast(restored)
}
