/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.impl

public interface DelayedInvokers {
    public fun add(invoker: () -> Unit)

    public class Simple: DelayedInvokers {
        private val invokers = ArrayList<() -> Unit>()

        override fun add(invoker: () -> Unit) {
            this.invokers.add(invoker)
        }

        public fun invoke() {
            this.invokers.forEach { it.invoke() }
            this.invokers.clear()
        }
    }
}