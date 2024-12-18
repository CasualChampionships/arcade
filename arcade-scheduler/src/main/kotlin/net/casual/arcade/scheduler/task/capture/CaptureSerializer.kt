/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.capture

import org.jetbrains.annotations.ApiStatus.Internal
import java.io.Serializable

@Internal
public interface CaptureSerializer<C, S: Serializable>: Serializable {
    public fun serialize(capture: C): S

    public fun deserialize(serialized: S): C

    private class Same<S: Serializable>: CaptureSerializer<S, S> {
        private fun readResolve(): Any {
            return instance
        }

        override fun serialize(capture: S): S {
            return capture
        }

        override fun deserialize(serialized: S): S {
            return serialized
        }
    }

    public companion object {
        private val instance = Same<Serializable>()

        public fun <S: Serializable> same(): CaptureSerializer<S, S> {
            @Suppress("UNCHECKED_CAST")
            return instance as CaptureSerializer<S, S>
        }
    }
}