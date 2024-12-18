/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.capture

import net.casual.arcade.scheduler.task.Task
import org.jetbrains.annotations.ApiStatus.Internal
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

@Internal
public class CaptureTask<C, K>(
    @Transient private var capture: C,
    private val mapper: CaptureMapper<C, K?>,
    private val serializer: CaptureSerializer<C, *>,
    private val task: CaptureConsumerTask<K>,
): Task, Serializable {
    init {
        // TODO: We should probably allow capturing primitives
        //   and allow other serializable captures, everything else
        //   should be disallowed
        // for (field in this.task::class.java.declaredFields) {
        //     if (Serializable::class.java.isAssignableFrom(field.type)) {
        //
        //     }
        // }
        // if (this.task::class.java.declaredFields.isNotEmpty()) {
        //     throw IllegalArgumentException("Serializable task may not capture outer variables")
        // }
    }

    override fun run() {
        val mapped = this.mapper.map(this.capture)
        if (mapped != null) {
            this.task.run(mapped)
        }
    }

    @Throws(IOException::class)
    private fun writeObject(stream: ObjectOutputStream) {
        stream.defaultWriteObject()
        stream.writeObject(this.serializer.serialize(this.capture))
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(stream: ObjectInputStream) {
        stream.defaultReadObject()
        @Suppress("UNCHECKED_CAST")
        val casted = this.serializer as CaptureSerializer<C, Serializable>
        this.capture = casted.deserialize(stream.readObject() as Serializable)
    }
}

