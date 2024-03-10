package net.casual.arcade.minigame.module

import com.google.gson.JsonObject
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.objOrNull
import net.casual.arcade.utils.JsonUtils.stringOrNull
import net.casual.arcade.utils.JsonUtils.strings
import net.casual.arcade.utils.JsonUtils.toJsonStringArray
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import java.util.*

public abstract class SavableMinigameModule<M: Minigame<M>, P: MinigameModule<M, P>>(
    minigame: M
): MinigameModule<M, P>(minigame) {
    /**
     * This method reads custom data for your module implementation
     * from your serialized [JsonObject].
     * Here you read all the data that you've serialized.
     *
     * While unlikely, you *should* consider irregular data that has
     * been modified by the user.
     *
     * @param json The [JsonObject] that was previously serialized.
     * @see writeData
     */
    @OverrideOnly
    protected abstract fun readData(json: JsonObject)

    /**
     * This method writes custom data for your module implementation
     * to a given [JsonObject].
     * Here you write all the data that you expect to deserialize.
     *
     * @param json The [JsonObject] that you are writing to.
     * @see readData
     */
    @OverrideOnly
    protected abstract fun writeData(json: JsonObject)

    internal fun serialize(): JsonObject {
        val json = JsonObject()
        json.addProperty("phase", this.phase.id)
        json.add("players", this.players.toJsonStringArray { it.toString() })

        val custom = JsonObject()
        this.writeData(custom)
        json.add("custom", custom)
        return json
    }

    internal fun deserialize(json: JsonObject) {
        val phaseId = json.stringOrNull("phase")
        var setPhase = false
        if (phaseId != null) {
            val phase = this.phases.firstOrNull { it.id == phaseId }
            if (phase != null) {
                this.phase = phase
                setPhase = true
            }
        }

        for (uuid in json.arrayOrDefault("players").strings()) {
            this.players.add(UUID.fromString(uuid))
        }

        val custom = json.objOrNull("custom")
        if (custom != null) {
            this.readData(custom)
        }

        if (setPhase) {
            for (phase in this.phases) {
                if (phase <= this.phase) {
                    phase.initialize(this.cast())
                }
            }
        }
    }
}