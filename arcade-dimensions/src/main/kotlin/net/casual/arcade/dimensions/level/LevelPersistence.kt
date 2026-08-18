/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level

import com.mojang.serialization.Codec
import net.casual.arcade.dimensions.utils.removeCustomLevel
import net.minecraft.util.StringRepresentable

public enum class LevelPersistence: StringRepresentable {
    /**
     * The level is never serialized and will be deleted after use.
     */
    Transient,

    /**
     * The level is serialized but will not be automatically
     * loaded when the server starts.
     * It will be deleted when manually removed
     * from the server via [removeCustomLevel].
     */
    @Deprecated("Temporary was renamed to 'Transient', double check if you meant to use Temporary", ReplaceWith("LevelPersistence.Transient"))
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

    /**
     * Whether this allows the level to be saved.
     */
    public fun shouldSave(): Boolean {
        return this != Transient
    }

    /**
     * Whether the level should be deleted when it is
     * removed with [removeCustomLevel].
     */
    public fun shouldDeleteOnRemove(): Boolean {
        return this == Transient || this == Temporary
    }

    override fun getSerializedName(): String {
        return this.name.lowercase()
    }

    public companion object {
        @JvmField
        public val CODEC: Codec<LevelPersistence> = StringRepresentable.fromEnum(LevelPersistence::values)
    }
}