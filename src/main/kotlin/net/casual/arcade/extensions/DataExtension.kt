package net.casual.arcade.extensions

import net.minecraft.nbt.Tag

interface DataExtension: Extension {
    fun getName(): String

    fun serialize(): Tag

    fun deserialize(element: Tag)
}