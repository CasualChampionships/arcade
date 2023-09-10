package net.casual.arcade.utils.impl

class ConcatenatedList<E> private constructor(
    private val first: List<E>,
    private val second: List<E>
): AbstractList<E>() {
    override val size: Int
        get() = this.first.size + this.second.size

    override fun get(index: Int): E {
        if (index >= this.first.size) {
            this.second[index - this.first.size]
        }
        return this.first[index]
    }

    companion object {
        /**
         * This method returns a concatenated view of both lists.
         *
         * This means that the elements of both lists aren't copied,
         * however, if the lists are mutable, then this list will
         * also be updated (since we are only viewing the two lists).
         *
         * @param E The type of the elements in the list.
         * @param other The other list to concatenate with.
         * @return The concatenated view of the lists.
         */
        fun <E> List<E>.concat(other: List<E>): List<E> {
            return ConcatenatedList(this, other)
        }
    }
}