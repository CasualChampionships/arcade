/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.impl

/**
 * Interface that allows the caller to add actions
 * which will be called at some later time.
 */
public interface DelayedActions {
    /**
     * Adds an action to be run later.
     *
     * @param action The action to run later.
     */
    public fun add(action: () -> Unit)

    public class Simple: DelayedActions {
        private val actions = ArrayList<() -> Unit>()

        override fun add(action: () -> Unit) {
            this.actions.add(action)
        }

        public fun run() {
            this.actions.forEach { it.invoke() }
            this.actions.clear()
        }
    }
}