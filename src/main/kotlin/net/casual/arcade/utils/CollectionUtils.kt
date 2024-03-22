package net.casual.arcade.utils

public object CollectionUtils {
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
}