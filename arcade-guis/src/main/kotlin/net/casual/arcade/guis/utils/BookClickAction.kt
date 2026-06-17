/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.utils

public sealed interface BookClickAction {
    public data object PreviousPage: BookClickAction
    public data object NextPage: BookClickAction
    public data object TakeBook: BookClickAction
    public data class SetPage(val page: Int): BookClickAction

    public companion object {
        public fun from(button: Int): BookClickAction? {
            if (button >= 100) {
                return SetPage(button - 100)
            }
            return when (button) {
                1 -> PreviousPage
                2 -> NextPage
                3 -> TakeBook
                else -> null
            }
        }
    }
}