/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker
import net.minecraft.gametest.framework.GameTestHelper
import java.lang.reflect.Method

/**
 * Base class for arcade game test suites.
 */
public abstract class ArcadeTestSuite: CustomTestMethodInvoker {
    override fun invokeTestMethod(helper: GameTestHelper, method: Method) {
        method.invoke(this, ArcadeTestContext(helper))
    }
}
