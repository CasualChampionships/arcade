package net.casual.arcade.minigame.task.impl

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.task.Task
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

public fun interface MinigameTask<M: Minigame<M>>: Serializable {
    public fun run(minigame: M)
}

@Suppress("FunctionName")
public fun <M: Minigame<M>> MinigameTask(minigame: M, task: MinigameTask<M>): Task {
    return Impl(minigame, task)
}

private class Impl<M: Minigame<M>>(
    @Transient private var minigame: M,
    private val task: MinigameTask<M>
): Task, Serializable {
    override fun run() {
        this.task.run(this.minigame)
    }

    @Throws(IOException::class)
    private fun writeObject(stream: ObjectOutputStream) {
        stream.defaultWriteObject()
        stream.writeObject(this.minigame.uuid)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(stream: ObjectInputStream) {
        stream.defaultReadObject()
        val uuid = stream.readObject() as UUID
        @Suppress("UNCHECKED_CAST")
        this.minigame = Minigames.get(uuid) as M
    }
}