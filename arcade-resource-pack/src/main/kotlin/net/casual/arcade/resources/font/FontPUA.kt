package net.casual.arcade.resources.font

/**
 * Enum representing the different private use areas available in Unicode.
 *
 * Most of the time [Plane0] should be enough, however if you need more
 * than `6400` characters then you can consider using [Plane15] or [Plane16].
 *
 * @see FontResources
 */
public enum class FontPUA(public val size: Int, public val codepoint: Int) {
    Plane0(6400, 0xE000),
    Plane15(65534, 0xF0000),
    Plane16(65534, 0x100000)
}