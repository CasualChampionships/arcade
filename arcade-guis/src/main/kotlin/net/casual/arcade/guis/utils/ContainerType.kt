/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.utils

import net.minecraft.world.inventory.MenuType

public enum class ContainerType(
    public val menu: MenuType<*>,
    public val slots: Int,
) {
    Generic9x1(MenuType.GENERIC_9x1, 9 * 1),
    Generic9x2(MenuType.GENERIC_9x2, 9 * 2),
    Generic9x3(MenuType.GENERIC_9x3, 9 * 3),
    Generic9x4(MenuType.GENERIC_9x4, 9 * 4),
    Generic9x5(MenuType.GENERIC_9x5, 9 * 5),
    Generic9x6(MenuType.GENERIC_9x6, 9 * 6),
    Generic3x3(MenuType.GENERIC_3x3, 3 * 3),
    ShulkerBox(MenuType.SHULKER_BOX, 9 * 3),
    Hopper(MenuType.HOPPER, 5 * 1);

    public companion object {
        public val Chest: ContainerType = Generic9x3
        public val DoubleChest: ContainerType = Generic9x6
        public val Barrel: ContainerType = Generic9x3
        public val Dropper: ContainerType = Generic3x3
        public val Dispenser: ContainerType = Generic3x3
    }
}