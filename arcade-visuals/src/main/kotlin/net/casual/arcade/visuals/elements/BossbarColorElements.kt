package net.casual.arcade.visuals.elements

import net.minecraft.world.BossEvent.BossBarColor

public object BossbarColorElements {
    private val PINK = UniversalElement.constant(BossBarColor.PINK)
    private val BLUE = UniversalElement.constant(BossBarColor.BLUE)
    private val RED = UniversalElement.constant(BossBarColor.RED)
    private val GREEN = UniversalElement.constant(BossBarColor.GREEN)
    private val YELLOW = UniversalElement.constant(BossBarColor.YELLOW)
    private val PURPLE = UniversalElement.constant(BossBarColor.PURPLE)
    private val WHITE = UniversalElement.constant(BossBarColor.WHITE)

    public fun of(colour: BossBarColor): PlayerSpecificElement<BossBarColor> {
        return when (colour) {
            BossBarColor.PINK -> PINK
            BossBarColor.BLUE -> BLUE
            BossBarColor.RED -> RED
            BossBarColor.GREEN -> GREEN
            BossBarColor.YELLOW -> YELLOW
            BossBarColor.PURPLE -> PURPLE
            BossBarColor.WHITE -> WHITE
        }
    }

    public fun pink(): PlayerSpecificElement<BossBarColor> {
        return PINK
    }

    public fun blue(): PlayerSpecificElement<BossBarColor> {
        return BLUE
    }

    public fun red(): PlayerSpecificElement<BossBarColor> {
        return RED
    }

    public fun green(): PlayerSpecificElement<BossBarColor> {
        return GREEN
    }

    public fun yellow(): PlayerSpecificElement<BossBarColor> {
        return YELLOW
    }

    public fun purple(): PlayerSpecificElement<BossBarColor> {
        return PURPLE
    }

    public fun white(): PlayerSpecificElement<BossBarColor> {
        return WHITE
    }
}