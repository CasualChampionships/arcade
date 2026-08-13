/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding

public class PathNodeHeap {
    private var nodes: Array<PathNode?> = arrayOfNulls(128)

    public var size: Int = 0
        private set

    public fun isEmpty(): Boolean {
        return this.size == 0
    }

    public fun insert(node: PathNode) {
        require(!node.isOpen()) { "Node $node is already in the heap" }

        if (this.size == this.nodes.size) {
            this.nodes = this.nodes.copyOf(this.size * 2)
        }
        this.nodes[this.size] = node
        node.heapIndex = this.size
        this.size++
        this.up(this.size - 1)
    }

    public fun pop(): PathNode {
        val first = this.nodes[0] ?: throw NoSuchElementException("Heap is empty")
        this.size--
        val last = this.nodes[this.size]
        this.nodes[this.size] = null
        first.heapIndex = -1

        if (this.size > 0) {
            this.nodes[0] = last
            last!!.heapIndex = 0
            this.down(0)
        }
        return first
    }

    public fun changeCost(node: PathNode, total: Double) {
        val previous = node.total
        node.total = total
        if (total < previous) {
            this.up(node.heapIndex)
        } else {
            this.down(node.heapIndex)
        }
    }

    public fun clear() {
        for (i in 0 until this.size) {
            this.nodes[i]?.heapIndex = -1
            this.nodes[i] = null
        }
        this.size = 0
    }

    private fun up(from: Int) {
        var index = from
        val node = this.nodes[index]!!
        val total = node.total
        while (index > 0) {
            val parentIndex = (index - 1) shr 1
            val parent = this.nodes[parentIndex]!!
            if (total >= parent.total) {
                break
            }
            this.nodes[index] = parent
            parent.heapIndex = index
            index = parentIndex
        }
        this.nodes[index] = node
        node.heapIndex = index
    }

    private fun down(from: Int) {
        var index = from
        val node = this.nodes[index]!!
        val total = node.total
        while (true) {
            val left = index * 2 + 1
            if (left >= this.size) {
                break
            }
            val right = left + 1
            var childIndex = left
            var child = this.nodes[left]!!
            if (right < this.size) {
                val other = this.nodes[right]!!
                if (other.total < child.total) {
                    childIndex = right
                    child = other
                }
            }
            if (child.total >= total) {
                break
            }
            this.nodes[index] = child
            child.heapIndex = index
            index = childIndex
        }
        this.nodes[index] = node
        node.heapIndex = index
    }
}
