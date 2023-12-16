package net.casual.arcade.database

public interface DatabaseWriter<E> {
    public fun createEmptyObject(): DatabaseObject<E>

    public fun createEmptyCollection(): DatabaseCollection<E>

    public fun createStringElement(string: String): DatabaseElement<E>

    public fun createDoubleElement(double: Double): DatabaseElement<E>

    public fun createLongElement(long: Long): DatabaseElement<E>

    public fun createIntegerElement(integer: Int): DatabaseElement<E>

    public fun createBooleanElement(boolean: Boolean): DatabaseElement<E>

    public fun createEmptyElement(): DatabaseElement<E>

    public fun createNumberElement(number: Number): DatabaseElement<E> {
        return when (number) {
            is Int -> this.createIntegerElement(number)
            is Long -> this.createLongElement(number)
            else -> this.createDoubleElement(number.toDouble())
        }
    }
}