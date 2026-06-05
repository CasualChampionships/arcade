/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.action

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.Identifier
import net.minecraft.server.dialog.action.CustomAll
import java.util.Optional

public class CustomAllActionBuilder: ActionBuilder() {
    private lateinit var _id: Identifier

    public var id: Identifier by this::_id

    private val additions = CompoundTag()

    public fun id(id: Identifier): CustomAllActionBuilder {
        this.id = id
        return this
    }

    public fun addParameter(key: String, value: Tag): CustomAllActionBuilder {
        this.additions.put(key, value)
        return this
    }

    override fun build(): CustomAll {
        require(this::_id.isInitialized) { "Id wasn't initialized" }
        return CustomAll(this.id, if (this.additions.isEmpty) Optional.empty() else Optional.of(this.additions))
    }
}