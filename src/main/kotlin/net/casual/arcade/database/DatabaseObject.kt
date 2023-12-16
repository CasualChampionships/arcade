package net.casual.arcade.database

public interface DatabaseObject<E>: DatabaseElement<E> {
    public fun write(property: String, element: DatabaseElement<E>)
}