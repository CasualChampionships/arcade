/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Deprecated("Get and use the optional instead")
public fun CompoundTag.contains(key: String, type: Byte): Boolean {
    val default = this.get(key) ?: return false
    return default.id == type
}

@Deprecated("Use provided method", ReplaceWith("this.getFloatOr(key, fallback)"))
public fun CompoundTag.floatOrDefault(key: String, fallback: Float): Float {
    return this.getFloatOr(key, fallback)
}

@Deprecated("Use provided method", ReplaceWith("this.getDoubleOr(key, fallback)"))
public fun CompoundTag.doubleOrDefault(key: String, fallback: Double): Double {
    return this.getDoubleOr(key, fallback)
}

@Deprecated("Use provided method", ReplaceWith("this.getIntOr(key, fallback)"))
public fun CompoundTag.intOrDefault(key: String, fallback: Int): Int {
    return this.getIntOr(key, fallback)
}

@Deprecated("Use provided method", ReplaceWith("this.getLongOr(key, fallback)"))
public fun CompoundTag.longOrDefault(key: String, fallback: Long): Long {
    return this.getLongOr(key, fallback)
}

@Deprecated("Use provided method", ReplaceWith("this.getStringOr(key, fallback)"))
public fun CompoundTag.stringOrDefault(key: String, fallback: String): String {
    return this.getStringOr(key, fallback)
}

@Deprecated("Use provided store method",
    ReplaceWith("this.store(key, ExtraCodecs.VECTOR3F, vec)", "net.minecraft.util.ExtraCodecs")
)
public fun CompoundTag.putVector3f(key: String, vec: Vector3f) {
    this.store(key, ExtraCodecs.VECTOR3F, vec)
}

@Deprecated("Use provided read method", ReplaceWith(
    "this.read(key, ExtraCodecs.VECTOR3F).getOrNull()",
    "net.minecraft.util.ExtraCodecs",
    "kotlin.jvm.optionals.getOrNull"
))
public fun CompoundTag.getVector3fOrNull(key: String): Vector3f? {
    return this.read(key, ExtraCodecs.VECTOR3F).getOrNull()
}

@Deprecated("Use provided store method",
    ReplaceWith("this.store(key, Vec3.CODEC, vec)", "net.minecraft.world.phys.Vec3")
)
public fun CompoundTag.putVec3(key: String, vec: Vec3) {
    this.store(key, Vec3.CODEC, vec)
}

@Deprecated("Use provided read method",
    ReplaceWith("this.read(key, Vec3.CODEC).get()", "net.minecraft.world.phys.Vec3")
)
public fun CompoundTag.getVec3(key: String): Vec3 {
    return this.read(key, Vec3.CODEC).get()
}

@Deprecated("Use provided read method", ReplaceWith(
    "this.read(key, Vec3.CODEC).getOrNull()",
    "net.minecraft.world.phys.Vec3",
    "kotlin.jvm.optionals.getOrNull"
))
public fun CompoundTag.getVec3OrNull(key: String): Vec3? {
    return this.read(key, Vec3.CODEC).getOrNull()
}

@Deprecated("Use provided store method", ReplaceWith(
    "this.store(key, ArcadeExtraCodecs.VEC2, vec)",
    "net.casual.arcade.utils.codec.ArcadeExtraCodecs"
))
public fun CompoundTag.putVec2(key: String, vec: Vec2) {
    this.store(key, ArcadeExtraCodecs.VEC2, vec)
}

@Deprecated("Use provided read method", ReplaceWith(
    "this.read(key, ArcadeExtraCodecs.VEC2).get()",
    "net.casual.arcade.utils.codec.ArcadeExtraCodecs"
))
public fun CompoundTag.getVec2(key: String): Vec2 {
    return this.read(key, ArcadeExtraCodecs.VEC2).get()
}

@Deprecated("Use provided read method", ReplaceWith(
    "this.read(key, ArcadeExtraCodecs.VEC2).getOrNull()",
    "net.casual.arcade.utils.codec.ArcadeExtraCodecs",
    "kotlin.jvm.optionals.getOrNull"
))
public fun CompoundTag.getVec2OrNull(key: String): Vec2? {
    return this.read(key, ArcadeExtraCodecs.VEC2).getOrNull()
}

@Deprecated("Use provided store method", ReplaceWith(
    "this.store(key, ResourceLocation.CODEC, id)",
    "net.minecraft.resources.ResourceLocation"
))
public fun CompoundTag.putId(key: String, id: ResourceLocation) {
    this.store(key, ResourceLocation.CODEC, id)
}

@Deprecated("Use provided read method", ReplaceWith(
    "this.read(key, ResourceLocation.CODEC).get()",
    "net.minecraft.resources.ResourceLocation"
))
public fun CompoundTag.getId(key: String): ResourceLocation {
    return this.read(key, ResourceLocation.CODEC).get()
}

@Deprecated("Use provided store method", ReplaceWith(
    "this.store(key, Location.CODEC, location)",
    "net.casual.arcade.utils.math.location.Location"
))
public fun CompoundTag.putLocation(key: String, location: Location) {
    this.store(key, Location.CODEC, location)
}

@Deprecated("Use provided read method",
    ReplaceWith("this.read(key, Location.CODEC).get()", "net.casual.arcade.utils.math.location.Location")
)
public fun CompoundTag.getLocation(key: String): Location {
    return this.read(key, Location.CODEC).get()
}

@Deprecated("Use provided store method", ReplaceWith(
    "this.store(key, LocationWithLevel.Resolvable.CODEC, location.resolvable())",
    "net.casual.arcade.utils.math.location.LocationWithLevel"
))
public fun CompoundTag.putLocationWithLevel(key: String, location: LocationWithLevel<ServerLevel>) {
    this.store(key, LocationWithLevel.Resolvable.CODEC, location.resolvable())
}

@Deprecated("Use provided read method", ReplaceWith(
    "this.read(key, LocationWithLevel.Resolvable.CODEC).map { it.resolve(server) }",
    "net.casual.arcade.utils.math.location.LocationWithLevel"
))
public fun CompoundTag.getLocation(key: String, server: MinecraftServer): Optional<LocationWithLevel<ServerLevel>> {
    return this.read(key, LocationWithLevel.Resolvable.CODEC).map { it.resolve(server) }
}