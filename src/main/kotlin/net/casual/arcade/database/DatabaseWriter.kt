package net.casual.arcade.database

public interface DatabaseWriter<E: DatabaseElement<E>> {
    public fun createEmptyObject(): DatabaseObject<E>

    public fun createEmptyCollection(): DatabaseCollection<E>

    public fun createStringElement(string: String): E

    public fun createNumberElement(number: Number): E

    public fun createBooleanElement(boolean: Boolean): E

    public fun createEmptyElement(): E
}