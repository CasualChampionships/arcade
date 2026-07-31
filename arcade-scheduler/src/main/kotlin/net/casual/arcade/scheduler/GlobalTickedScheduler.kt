/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.client.ClientStoppingEvent
import net.casual.arcade.events.client.ClientTickEvent
import net.casual.arcade.events.phase.BuiltInEventPhases.POST
import net.casual.arcade.events.server.ServerStopEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.side.LogicalSide
import net.casual.arcade.utils.time.MinecraftTimeDuration
import java.util.concurrent.CopyOnWriteArrayList

/**
 * This is a global implementation of a [SimpleTickedScheduler], you
 * can schedule any [Task]s here, and they will be run later.
 *
 * However, it is advised that you use your own [TickedScheduler]
 * as it allows you more flexibility.
 *
 * @see TickedScheduler
 * @see SimpleTickedScheduler
 */
public sealed class GlobalTickedScheduler(
    final override val target: LogicalSide
): TickedScheduler {
    private val schedulers = CopyOnWriteArrayList<SimpleTickedScheduler>()
    private val scheduler = SimpleTickedScheduler(this.target)

    public fun get(): TickedScheduler {
        return this.scheduler
    }

    /**
     * This method will schedule a [task] to be run later in
     * the tick.
     * This is useful if you need to execute something after it
     * has been initialized.
     *
     * @param task The runnable to be scheduled.
     */
    public fun later(task: Task) {
        this.schedule(MinecraftTimeDuration.ZERO, task)
    }

    /**
     * This method will schedule a [task] to be run
     * after a given [delay].
     *
     * @param delay The duration to wait before running the [task].
     * @param task The runnable to be scheduled.
     */
    override fun schedule(delay: MinecraftTimeDuration, task: Task) {
        this.scheduler.schedule(delay, task)
    }

    /**
     * This creates a [SimpleTickedScheduler] which will be ticked
     * alongside this one for the given [lifetime], after which it
     * is removed and all its remaining tasks are cancelled.
     *
     * @param lifetime How long the scheduler should be ticked for.
     * @return The temporary scheduler.
     */
    public fun temporaryScheduler(lifetime: MinecraftTimeDuration): SimpleTickedScheduler {
        val temporary = SimpleTickedScheduler(this.target)
        this.schedulers.add(temporary)
        this.schedule(lifetime + 1.Ticks) {
            this.schedulers.remove(temporary)
            temporary.cancelAll()
        }
        return temporary
    }

    protected fun tick() {
        this.scheduler.tick()
        for (scheduler in this.schedulers) {
            scheduler.tick()
        }
    }

    protected fun stop() {
        this.scheduler.cancelAll()
        for (scheduler in this.schedulers) {
            scheduler.cancelAll()
        }
        this.schedulers.clear()
    }

    private object ServerScheduler: GlobalTickedScheduler(LogicalSide.Server) {
        fun load() {
            GlobalEventHandler.Server.register<ServerTickEvent>(phase = POST) { this.tick() }
            GlobalEventHandler.Server.register<ServerStopEvent>(phase = POST) { this.stop() }
        }
    }

    private object ClientScheduler: GlobalTickedScheduler(LogicalSide.Client) {
        fun load() {
            GlobalEventHandler.Client.register<ClientTickEvent>(phase = POST) { this.tick() }
            GlobalEventHandler.Client.register<ClientStoppingEvent> { this.stop() }
        }
    }

    public companion object {
        /**
         * The global scheduler ticked by the server.
         */
        @JvmField
        public val Server: GlobalTickedScheduler = ServerScheduler

        /**
         * The global scheduler ticked by the client.
         */
        @JvmField
        public val Client: GlobalTickedScheduler = ClientScheduler

        internal fun loadServer() {
            ServerScheduler.load()
        }

        internal fun loadClient() {
            ClientScheduler.load()
        }
    }
}
