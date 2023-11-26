package net.casual.arcade.task.impl

import net.casual.arcade.task.Task
import net.casual.arcade.utils.PlayerUtils
import net.minecraft.server.level.ServerPlayer
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

public interface PlayerTask: Serializable {
    public fun run(player: ServerPlayer)
}

@Suppress("FunctionName")
public fun PlayerTask(player: ServerPlayer, task: PlayerTask): Task {
    return PlayerTaskImpl(player.uuid, task)
}

private class PlayerTaskImpl(
    @Transient private var uuid: UUID,
    private val task: PlayerTask
): Task, Serializable {
    override fun run() {
        val player = PlayerUtils.player(this.uuid) ?: return
        this.task.run(player)
    }

    @Throws(IOException::class)
    private fun writeObject(stream: ObjectOutputStream) {
        stream.defaultWriteObject()
        stream.writeObject(this.uuid)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(stream: ObjectInputStream) {
        stream.defaultReadObject()
        this.uuid = stream.readObject() as UUID
    }
}