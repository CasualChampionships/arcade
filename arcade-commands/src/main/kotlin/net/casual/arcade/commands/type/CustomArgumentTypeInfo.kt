/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.type

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.ArgumentType
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.synchronization.ArgumentTypeInfo
import net.minecraft.network.FriendlyByteBuf

public abstract class CustomArgumentTypeInfo<A: ArgumentType<*>>: ArgumentTypeInfo<A, CustomArgumentTypeInfo<A>.Template> {
    public abstract fun getFacadeType(): Class<out ArgumentType<*>>

    public open fun serializeTypeData(buffer: FriendlyByteBuf) {

    }

    final override fun serializeToNetwork(template: Template, buffer: FriendlyByteBuf) {
        this.serializeTypeData(buffer)
    }

    final override fun unpack(argument: A): Template {
        return Template()
    }

    final override fun deserializeFromNetwork(buffer: FriendlyByteBuf): Template {
        throw UnsupportedOperationException()
    }

    final override fun serializeToJson(template: Template, json: JsonObject) {
        throw UnsupportedOperationException()
    }

    public inner class Template: ArgumentTypeInfo.Template<A> {
        override fun instantiate(context: CommandBuildContext): A {
            throw UnsupportedOperationException()
        }

        override fun type(): ArgumentTypeInfo<A, *> {
            return this@CustomArgumentTypeInfo
        }
    }

    public companion object {
        public fun <A: ArgumentType<*>> of(type: Class<A>): CustomArgumentTypeInfo<A> {
            return object: CustomArgumentTypeInfo<A>() {
                override fun getFacadeType(): Class<out ArgumentType<*>> {
                    return type
                }
            }
        }
    }
}