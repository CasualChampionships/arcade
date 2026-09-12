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
import net.casual.arcade.utils.coroutine.joinBlocking
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteRecursively
import kotlin.time.Duration.Companion.seconds

public fun <M: Minigame> TestContext.minigame(
    constructor: (MinecraftServer, UUID) -> M
): TestMinigameBuilder<M> {
    return TestMinigameBuilder(this, constructor)
}

public fun TestContext.track(minigame: Minigame) {
    this.track(AutoCloseable(minigame::close))
}

public fun <M> TestContext.reload(minigame: M): M where M: Minigame, M: SerializableMinigame {
    val copy = saveCopy(minigame)
    minigame.close()

    val restored = Minigames.read(copy, this.server)
    this.track(restored)

    val type = minigame.javaClass
    this.assertTrue(type.isInstance(restored), "Restored minigame was not a ${type.simpleName}")
    return type.cast(restored)
}

@OptIn(ExperimentalPathApi::class)
public fun TestContext.copySave(minigame: Minigame): Path {
    val copy = this.server.getWorldPath(LevelResource.ROOT)
        .resolve("gametest-minigames")
        .resolve("instances")
        .resolve(UUID.randomUUID().toString())
    copy.createParentDirectories()
    minigame.getSavePath().copyToRecursively(copy, followLinks = false)

    this.track(AutoCloseable(copy::deleteRecursively))
    return copy
}

private fun <M> TestContext.saveCopy(minigame: M): Path where M: Minigame, M: SerializableMinigame {
    minigame.save().joinBlocking(1.seconds)
    return this.copySave(minigame)
}
