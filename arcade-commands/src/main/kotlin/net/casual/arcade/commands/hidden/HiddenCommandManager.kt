package net.casual.arcade.commands.hidden

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.*

public object HiddenCommandManager {
    private const val ROOT = "~arcade\$hidden\$command"

    private val commands = Object2ObjectOpenHashMap<String, HiddenCommand>()

    private val deletion = Int2ObjectOpenHashMap<ArrayList<String>>()
    private var ticks = 0

    public fun register(timeout: MinecraftTimeDuration, command: HiddenCommand): String {
        val name = "$ROOT ${UUID.randomUUID()}"
        this.commands[name] = command
        this.deletion[this.ticks + timeout.ticks]
        return "/$name"
    }

    @Internal
    @JvmStatic
    public fun tick() {
        val commands = this.deletion[this.ticks++] ?: return
        for (command in commands) {
            this.commands.remove(command)
        }
    }

    @Internal
    @JvmStatic
    public fun onCommand(player: ServerPlayer, name: String): Boolean {
        val command = this.commands[name] ?: return false
        val context = HiddenCommandContext(player)
        command.run(context)
        if (context.removed()) {
            this.commands.remove(name)
        }
        return true
    }
}