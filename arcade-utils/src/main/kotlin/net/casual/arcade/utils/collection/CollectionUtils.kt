/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.collection

import com.google.common.collect.Multimap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap

public fun <E> List<E>.concat(other: List<E>): List<E> {
    return ConcatenatedList.concat(this, other)
}

public fun <E> List<E>.concat(vararg other: E): List<E> {
    return ConcatenatedList.concat(this, other.asList())
}

public fun <E: Comparable<E>> MutableList<E>.mergeSorted(sorted: List<E>) {
    if (sorted.isEmpty()) {
        return
    }
    if (this.isEmpty()) {
        this.addAll(sorted)
        return
    }

    val result = ArrayList<E>(this.size + sorted.size)
    var i = 0
    var j = 0
    while (i < this.size && j < sorted.size) {
        val a = this[i]
        val b = sorted[j]
        if (a <= b) {
            result.add(a)
            i++
        } else {
            result.add(b)
            j++
        }
    }

    while (i < this.size) {
        result.add(this[i++])
    }
    while (j < sorted.size) {
        result.add(sorted[j++])
    }

    this.clear()
    this.addAll(result)
}


@Deprecated("Use mergeSorted instead")
public fun <E: Comparable<E>> MutableList<E>.addSorted(sorted: List<E>) {
    if (sorted.isEmpty()) {
        return
    }
    if (this.isEmpty()) {
        this.addAll(sorted)
        return
    }

    var i = 0
    var j = 0
    while (i < this.size && j < sorted.size) {
        val current = this[i]
        val other = sorted[j]
        if (current > other) {
            this.add(i, other)
            j++
        } else {
            i++
        }
    }

    while (j < sorted.size) {
        this.add(sorted[j++])
    }
}

public fun <T> MutableList<T>.resizeAndFill(newSize: Int, defaultValue: T) {
    if (newSize > this.size) {
        for (i in this.size ..< newSize) {
            this.add(defaultValue)
        }
    } else if (newSize < this.size) {
        this.subList(newSize, this.size).clear()
    }
}

public fun <T> Iterable<T>.cycle(): Sequence<T> = sequence {
    while (true) {
        for (item in this@cycle) {
            yield(item)
        }
    }
}

public fun <T> Iterator<T>.asMutable(): MutableIterator<T> {
    if (this is MutableIterator<T>) {
        return this
    }
    val wrapped = this
    return object: MutableIterator<T> {
        override fun hasNext() = wrapped.hasNext()
        override fun next() = wrapped.next()
        override fun remove() = throw UnsupportedOperationException("remove not supported")
    }
}

public fun <K, V> Map<K, V>.deduplicateValues(): MutableMap<K, V> {
    return this.deduplicateValuesTo(LinkedHashMap())
}

public fun <K, V> Map<K, V>.deduplicateValuesTo(destination: MutableMap<K, V>): MutableMap<K, V> {
    val cache = Object2ObjectOpenHashMap<V, V>(this.size)
    return this.mapValuesTo(destination) { (_, value) -> cache.getOrPut(value) { value } }
}

public fun <K, V> Multimap<K, V>.putAll(other: Map<K, Collection<V>>) {
    for ((k, c) in other) {
        this.putAll(k, c)
    }
}