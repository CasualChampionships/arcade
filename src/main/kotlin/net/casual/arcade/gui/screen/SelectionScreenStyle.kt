package net.casual.arcade.gui.screen

import net.minecraft.util.Mth

/**
 * This interface allows you to configure how
 * items are laid out in a selection screen.
 *
 * @see SelectionScreen
 */
public interface SelectionScreenStyle {
    public fun getSlots(): Collection<Int>

    private class CenteredStyle(
        private val width: Int,
        private val height: Int
    ): SelectionScreenStyle {
        private val slots by lazy {
            val slots = ArrayList<Int>(this.width * this.height)
            val startHeight = (5 - this.height) / 2
            val startWidth = (9 - this.width) / 2
            for (height in startHeight until startHeight + this.height) {
                for (width in startWidth until startWidth + this.width) {
                    slots.add(9 * height + width)
                }
            }
            slots
        }

        override fun getSlots(): Collection<Int> {
            return this.slots
        }
    }

    public companion object {
        public val DEFAULT: SelectionScreenStyle = object: SelectionScreenStyle {
            private val slots = (0 until 45).toList()

            override fun getSlots(): Collection<Int> {
                return this.slots
            }
        }

        public fun centered(width: Int, height: Int): SelectionScreenStyle {
            if (width > 9 || width < 0) {
                throw IllegalArgumentException("Invalid width argument $width")
            }
            if (height > 5 || height < 0) {
                throw IllegalArgumentException("Invalid height argument $height")
            }
            return CenteredStyle(width, height)
        }

        public fun centered(elements: Int): SelectionScreenStyle {
            if (elements == 2) {
                return bool()
            }
            for (i in 1 .. 5) {
                val remainder = elements / i
                if (remainder <= 9) {
                    val columns = Mth.ceil((remainder + (i - 1) * 9) / i.toDouble())
                    return centered(columns, i)
                }
            }
            return DEFAULT
        }

        public fun bool(): SelectionScreenStyle {
            return object: SelectionScreenStyle {
                override fun getSlots(): Collection<Int> {
                    return listOf(21, 23)
                }
            }
        }
    }
}