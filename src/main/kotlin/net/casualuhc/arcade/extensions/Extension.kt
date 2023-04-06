package net.casualuhc.arcade.extensions

import net.minecraft.nbt.Tag

interface Extension {
    fun getName(): String

    fun serialize(): Tag

    fun deserialize(element: Tag)
}