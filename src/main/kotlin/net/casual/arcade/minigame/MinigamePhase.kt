package net.casual.arcade.minigame

interface MinigamePhase {
    val id: String
    val ordinal: Int

    operator fun compareTo(other: MinigamePhase): Int {
        return this.ordinal.compareTo(other.ordinal)
    }

    private class None: MinigamePhase {
        override val id: String = "none"
        override val ordinal: Int = -1
    }

    companion object {
        val NONE: MinigamePhase = None()
    }
}