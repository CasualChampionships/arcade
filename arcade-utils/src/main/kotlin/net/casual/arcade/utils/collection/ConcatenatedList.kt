/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.collection

public class ConcatenatedList<E> private constructor(
    private val lists: MutableList<List<E>>
): AbstractList<E>() {
    override val size: Int
        get() = this.lists.sumOf { it.size }

    override fun get(index: Int): E {
        var passed = 0

        for (list in this.lists) {
            if (index < passed + list.size) {
                return list[index - passed]
            }
            passed += list.size
        }
        throw IndexOutOfBoundsException()
    }

    public companion object {
        /**
         * This method returns a concatenated view of the lists.
         *
         * This means that the elements of the lists aren't copied,
         * however, if the lists are mutable, then this list will
         * also be updated (since we are only viewing the lists).
         *
         * @param E The type of the elements in the list.
         * @param other The other list to concatenate with.
         * @return The concatenated view of the lists.
         */
        @JvmStatic
        public fun <E> concat(first: List<E>, other: List<E>): List<E> {
            if (first is ConcatenatedList) {
                first.lists.add(other)
                return first
            }
            if (other is ConcatenatedList) {
                other.lists.add(0, first)
                return other
            }
            return ConcatenatedList(mutableListOf(first, other))
        }

        /**
         * Concatenates a vararg number of other elements.
         *
         * @param E The type of elements in the list.
         * @param other The other elements to concatenate with.
         * @return The concatenated view of the lists.
         */
        @JvmStatic
        public fun <E> concat(first: List<E>, vararg other: E): List<E> {
            if (other.isEmpty()) {
                return first
            }
            return this.concat(first, other.asList())
        }
    }
}