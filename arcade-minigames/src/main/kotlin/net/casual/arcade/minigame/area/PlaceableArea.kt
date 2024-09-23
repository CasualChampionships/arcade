package net.casual.arcade.minigame.area

public interface PlaceableArea: Area {
    public fun place(): Boolean

    public fun replace(): Boolean {
        this.removeAllButPlayers()
        return this.place()
    }
}