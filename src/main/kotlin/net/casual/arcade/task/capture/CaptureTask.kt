package net.casual.arcade.task.capture

import net.casual.arcade.task.Task
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

public class CaptureTask<C, K>(
    @Transient private var capture: C,
    private val mapper: CaptureMapper<C, K?>,
    private val serializer: CaptureSerializer<C, *>,
    public val task: CaptureConsumerTask<K>,
): Task, Serializable {
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

