package net.casual.arcade.utils

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.FloatTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.jetbrains.annotations.Contract
import org.joml.Vector3f
import org.joml.Vector3fc

public object TagUtils {
    public fun CompoundTag.contains(key: String, type: Byte): Boolean {
        return this.contains(key, type.toInt())
    }

    public fun CompoundTag.floatOrDefault(key: String, fallback: Float): Float {
        if (this.contains(key, Tag.TAG_ANY_NUMERIC)) {
            return this.getFloat(key)
        }
        return fallback
    }

    public fun CompoundTag.doubleOrDefault(key: String, fallback: Double): Double {
        if (this.contains(key, Tag.TAG_ANY_NUMERIC)) {
            return this.getDouble(key)
        }
        return fallback
    }

    public fun CompoundTag.intOrDefault(key: String, fallback: Int): Int {
        if (this.contains(key, Tag.TAG_ANY_NUMERIC)) {
            return this.getInt(key)
        }
        return fallback
    }

    public fun CompoundTag.longOrDefault(key: String, fallback: Long): Long {
        if (this.contains(key, Tag.TAG_ANY_NUMERIC)) {
            return this.getLong(key)
        }
        return fallback
    }

    public fun CompoundTag.stringOrDefault(key: String, fallback: String): String {
        if (this.contains(key, Tag.TAG_STRING)) {
            return this.getString(key)
        }
        return fallback
    }

    public fun CompoundTag.putVector3f(key: String, vec: Vector3fc) {
        val list = ListTag()
        list.add(FloatTag.valueOf(vec.x()))
        list.add(FloatTag.valueOf(vec.y()))
        list.add(FloatTag.valueOf(vec.z()))
        this.put(key, list)
    }

    public fun CompoundTag.getVector3fOrNull(key: String): Vector3f? {
        if (this.contains(key, Tag.TAG_LIST)) {
            val list = this.getList(key, Tag.TAG_ANY_NUMERIC.toInt())
            if (list.size == 3) {
                return Vector3f(list.getFloat(0), list.getFloat(1), list.getFloat(2))
            }
        }
        return null
    }

    public fun CompoundTag.putVec3(key: String, vec: Vec3) {
        val list = ListTag()
        list.add(DoubleTag.valueOf(vec.x))
        list.add(DoubleTag.valueOf(vec.y))
        list.add(DoubleTag.valueOf(vec.z))
        this.put(key, list)
    }

    public fun CompoundTag.getVec3OrNull(key: String): Vec3? {
        if (this.contains(key, Tag.TAG_LIST)) {
            val list = this.getList(key, Tag.TAG_ANY_NUMERIC.toInt())
            if (list.size == 3) {
                return Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2))
            }
        }
        return null
    }

    public fun CompoundTag.putVec2(key: String, vec: Vec2) {
        val list = ListTag()
        list.add(FloatTag.valueOf(vec.x))
        list.add(FloatTag.valueOf(vec.y))
        this.put(key, list)
    }

    public fun CompoundTag.getVec2OrNull(key: String): Vec2? {
        if (this.contains(key, Tag.TAG_LIST)) {
            val list = this.getList(key, Tag.TAG_ANY_NUMERIC.toInt())
            if (list.size == 2) {
                return Vec2(list.getFloat(0), list.getFloat(1))
            }
        }
        return null
    }
}