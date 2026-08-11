/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.utils

import net.casual.arcade.gametest.TestSuite
import org.reflections.Reflections
import org.reflections.scanners.Scanners

public object TestUtils {
    public fun findTestSuiteObjects(packagePrefix: String): Set<TestSuite> {
        val reflections = Reflections(packagePrefix, Scanners.SubTypes)
        val subcommands = reflections.getSubTypesOf(TestSuite::class.java)
            .mapNotNull { clazz -> clazz.kotlin.objectInstance }
            .toSet()
        return subcommands
    }
}