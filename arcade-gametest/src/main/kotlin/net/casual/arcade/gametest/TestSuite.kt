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
public abstract class TestSuite: CustomTestMethodInvoker {
    /**
     * The namespace for the tests in this test suite.
     * If left blank then the mod id of the entrypoint
     * provider is used as the namespace.
     */
    public open val namespace: String get() = ""

    /**
     * This is prepended to your method name to form the
     * test name. You may use a blank prefix, in which case
     * you must ensure that all method names under the
     * namespace are unique.
     */
    public open val prefix: String = this.javaClass.simpleName

    override fun invokeTestMethod(helper: GameTestHelper, method: Method) {
        method.invoke(this, TestContext(helper))
    }
}
