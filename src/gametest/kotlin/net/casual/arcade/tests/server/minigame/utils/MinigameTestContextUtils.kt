/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame.utils

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.gametest.minigame.TestMinigameBuilder
import net.casual.arcade.gametest.minigame.minigame

fun TestContext.minigame(): TestMinigameBuilder<TestMinigame> {
    return this.minigame(::TestMinigame)
}

fun TestMinigameBuilder<TestMinigame>.score(score: Int): TestMinigameBuilder<TestMinigame> {
    return this.configure { it.score = score }
}

fun TestMinigameBuilder<TestMinigame>.withLevel(): TestMinigameBuilder<TestMinigame> {
    return this.configure(TestMinigame::addLevel)
}
