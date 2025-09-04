/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization.json

import com.google.gson.JsonObject
import net.minecraft.nbt.CompoundTag

/**
 * Analogous to [CompoundTag.merge], it recursively
 * merges two json objects together.
 *
 * @param other The json object to merge with.
 * @return The merged json object, same instance as [this].
 */
public fun JsonObject.merge(other: JsonObject): JsonObject {
    for ((key, value) in other.asMap()) {
        if (value is JsonObject) {
            val counterpart = this.get(key)
            if (counterpart is JsonObject) {
                counterpart.merge(value)
                continue
            }
        }

        this.add(key, value)
    }

    return this
}