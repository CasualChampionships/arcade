package net.casual.arcade.minigame.phase

public interface Phased<M> {
    public val phase: Phase<M>

    /**
     * Checks whether the object is in a given phase.
     *
     * @param phase The phase to check whether the object is in.
     * @return Whether the object is in that phase.
     */
    public fun isPhase(phase: Phase<M>): Boolean {
        return this.phase == phase
    }

    /**
     * Checks whether the object is before a given phase.
     *
     * @param phase The phase to check whether the object is before.
     * @return Whether the object is before that phase.
     */
    public fun isBeforePhase(phase: Phase<M>): Boolean {
        return this.phase < phase
    }

    /**
     * Checks whether the object is past a given phase.
     *
     * @param phase The phase to check whether the object has past.
     * @return Whether the object is past that phase.
     */
    public fun isAfterPhase(phase: Phase<M>): Boolean {
        return this.phase > phase
    }
}