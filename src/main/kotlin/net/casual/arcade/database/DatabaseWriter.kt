package net.casual.arcade.database

public interface DatabaseWriter<E> {
    public fun createEmptyObject(): DatabaseObject<E>

    public fun createEmptyCollection(): DatabaseCollection<E>

    public fun createStringElement(string: String): DatabaseElement<E>

    public fun createNumberElement(number: Number): DatabaseElement<E>

    public fun createBooleanElement(boolean: Boolean): DatabaseElement<E>

    public fun createEmptyElement(): DatabaseElement<E>
}