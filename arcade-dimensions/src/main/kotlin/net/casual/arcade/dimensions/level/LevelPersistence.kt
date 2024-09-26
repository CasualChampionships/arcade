package net.casual.arcade.dimensions.level

import com.mojang.serialization.Codec
import net.casual.arcade.dimensions.utils.removeCustomLevel
import net.minecraft.util.StringRepresentable

public enum class LevelPersistence: StringRepresentable {
    /**
     * The level will be deleted after use.
     */
    Temporary,

    /**
     * The level will be serialized but will not
     * be automatically loaded when the server starts.
     */
    Permanent,

    /**
     * The level will be serialized *and* will be
     * automatically loaded when the server starts.
     *
     * The level will only be automatically loaded
     * if it was not unloaded manually with
     * [removeCustomLevel].
     */
    Persistent;

    public fun shouldSave(): Boolean {
        return this != Temporary
    }

    override fun getSerializedName(): String {
        return this.name.lowercase()
    }

    public companion object {
        public val CODEC: Codec<LevelPersistence> = StringRepresentable.fromEnum(LevelPersistence::values)
    }
}