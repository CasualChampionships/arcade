/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.action

import com.mojang.serialization.JavaOps
import net.minecraft.server.dialog.action.CommandTemplate
import net.minecraft.server.dialog.action.ParsedTemplate

public class CommandTemplateActionBuilder: ActionBuilder() {
    private lateinit var _template: ParsedTemplate

    public var template: ParsedTemplate by this::_template

    public fun template(template: ParsedTemplate): CommandTemplateActionBuilder {
        this.template = template
        return this
    }

    public fun template(template: String): CommandTemplateActionBuilder {
        this.template = ParsedTemplate.CODEC.parse(JavaOps.INSTANCE, template).orThrow
        return this
    }

    override fun build(): CommandTemplate {
        require(this::_template.isInitialized) { "Template wasn't initialized" }
        return CommandTemplate(this.template)
    }
}