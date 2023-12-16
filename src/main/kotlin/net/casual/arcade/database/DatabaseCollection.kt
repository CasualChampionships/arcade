package net.casual.arcade.database

public interface DatabaseCollection<E>: DatabaseElement<E> {
    public fun add(element: DatabaseElement<E>)
}