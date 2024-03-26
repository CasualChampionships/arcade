package net.casual.arcade.gui.elements

public object BooleanElements {
    private val TRUE = UniversalElement.constant(true)
    private val FALSE = UniversalElement.constant(false)

    public fun of(boolean: Boolean): PlayerSpecificElement<Boolean> {
        return if (boolean) TRUE else FALSE
    }

    public fun alwaysTrue(): PlayerSpecificElement<Boolean> {
        return TRUE
    }

    public fun alwaysFalse(): PlayerSpecificElement<Boolean> {
        return FALSE
    }
}