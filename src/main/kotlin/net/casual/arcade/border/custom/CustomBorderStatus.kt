package net.casual.arcade.border.custom



public enum class CustomBorderStatus(color: Int) {

    GROWING(4259712),
    SHRINKING(16724016),
    STATIONARY(2138367),
    //Moving should only be selected if it would otherwise be stationary. TODO: TBD, remove this?
    MOVING(5);


}