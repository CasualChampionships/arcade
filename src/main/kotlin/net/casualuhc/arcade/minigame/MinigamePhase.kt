package net.casualuhc.arcade.minigame

interface MinigamePhase: Comparable<MinigamePhase> {
    val id: String
    val ordinal: Int

    override fun compareTo(other: MinigamePhase): Int {
        return this.ordinal.compareTo(other.ordinal)
    }

    companion object {
        val NONE = object: MinigamePhase {
            override val id: String
                get() = "none"
            override val ordinal: Int
                get() = -1
        }
    }
}