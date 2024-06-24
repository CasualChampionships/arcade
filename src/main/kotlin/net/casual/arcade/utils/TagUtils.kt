package net.casual.arcade.utils

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.phys.Vec3
import org.jetbrains.annotations.Contract

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

    public fun CompoundTag.readVec3OrNull(key: String): Vec3? {
        if (this.contains(key, Tag.TAG_LIST)) {
            val list = this.getList(key, Tag.TAG_DOUBLE.toInt())
            if (list.size == 3) {
                return Vec3(list.getDouble(0), list.getDouble(0), list.getDouble(0))
            }
        }
        return null
    }
}