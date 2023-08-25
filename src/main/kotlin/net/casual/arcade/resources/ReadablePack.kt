package net.casual.arcade.resources

import java.io.InputStream

interface ReadablePack {
    val name: String

    fun readable(): Boolean

    fun length(): Long

    fun stream(): InputStream
}