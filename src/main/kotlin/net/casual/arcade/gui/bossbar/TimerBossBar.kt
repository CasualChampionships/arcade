package net.casual.arcade.gui.bossbar

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.gui.TickableUI
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.task.Completable
import net.casual.arcade.task.Task
import net.casual.arcade.task.serialization.TaskCreationContext
import net.casual.arcade.task.serialization.TaskWriteContext
import net.casual.arcade.utils.JsonUtils.array
import net.casual.arcade.utils.JsonUtils.boolean
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.ints
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.TimeUtils.Ticks
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

public abstract class TimerBossBar: CustomBossBar(), TickableUI, Completable {
    private val completable = Completable.Impl()

    private var ticks = -1
    private var tick = 0

    override val complete: Boolean
        get() = this.completable.complete

    public val hasDuration: Boolean
        get() = this.ticks != -1

    override fun tick(server: MinecraftServer) {
        if (this.ticks == -1) {
            return
        }
        if (this.tick < this.ticks) {
            this.tick++
            return
        }
        this.completable.complete()
    }

    override fun then(task: Task): Completable {
        return this.completable.then(task)
    }

    public fun setDuration(duration: MinecraftTimeDuration) {
        this.completable.complete = false
        this.tick = 0
        this.ticks = duration.ticks
    }

    public fun setRemainingDuration(duration: MinecraftTimeDuration) {
        if (!this.hasDuration) {
            return
        }
        this.tick = this.ticks - duration.ticks
    }

    public fun removeDuration() {
        this.completable.complete = true
        this.ticks = -1
    }

    public fun getProgress(): Float {
        return if (!this.hasDuration) 0.0F else this.tick / this.ticks.toFloat()
    }

    public fun getRemainingDuration(): MinecraftTimeDuration {
        return if (!this.hasDuration) 0.Ticks else (this.ticks - this.tick).Ticks
    }

    /**
     * This gets the progress of the [CustomBossBar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the progress.
     * @return The progress to display the bossbar as having.
     */
    override fun getProgress(player: ServerPlayer): Float {
        return this.getProgress()
    }

    public fun writeData(context: TaskWriteContext): JsonObject {
        val data = JsonObject()
        data.addProperty("tick", this.tick)
        data.addProperty("ticks", this.ticks)
        data.addProperty("complete", this.complete)

        val taskArray = JsonArray()
        for (task in this.completable.tasks()) {
            taskArray.add(context.writeTask(task) ?: continue)
        }
        data.add("tasks", taskArray)

        return data
    }

    public fun readData(context: TaskCreationContext) {
        val data = context.getCustomData()
        this.tick = data.int("tick")
        this.ticks = data.int("ticks")
        this.completable.complete = data.boolean("complete")

        for (taskData in data.array("tasks").ints()) {
            val task = context.createTask(taskData) ?: continue
            this.completable.then(task)
        }
    }
}