/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.utils

import net.casual.arcade.utils.component.join
import net.casual.arcade.utils.registries.isOf
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.network.Filterable
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.WrittenBookContent

private val DEFAULT_CONTENT = WrittenBookContent(Filterable.passThrough(""), "", 0, listOf(), false);

public fun ItemStack.getBookPageCount(): Int {
    val content = this.get(DataComponents.WRITTEN_BOOK_CONTENT) ?: return 0
    return content.pages().size
}

public fun ItemStack.addBookPage(vararg lines: Component): ItemStack {
    val content = this.getOrDefault(DataComponents.WRITTEN_BOOK_CONTENT, DEFAULT_CONTENT)
    val pages: List<Filterable<Component>> = content.pages() + Filterable.passThrough(compactToPage(lines))
    val updated = WrittenBookContent(content.title, content.author, content.generation, pages, content.resolved)
    this.set(DataComponents.WRITTEN_BOOK_CONTENT, updated)
    return this
}

public fun ItemStack.setBookPage(index: Int, vararg lines: Component): ItemStack {
    val content = this.getOrDefault(DataComponents.WRITTEN_BOOK_CONTENT, DEFAULT_CONTENT)
    val pages = content.pages().toMutableList()
    pages[index] = Filterable.passThrough(compactToPage(lines))
    val updated = WrittenBookContent(content.title, content.author, content.generation, pages, content.resolved)
    this.set(DataComponents.WRITTEN_BOOK_CONTENT, updated)
    return this
}

public fun ItemStack.setBookAuthor(author: String): ItemStack {
    val content = this.getOrDefault(DataComponents.WRITTEN_BOOK_CONTENT, DEFAULT_CONTENT)
    val updated = WrittenBookContent(content.title, author, content.generation, content.pages(), content.resolved)
    this.set(DataComponents.WRITTEN_BOOK_CONTENT, updated)
    return this
}

public fun ItemStack.setBookTitle(title: String): ItemStack {
    val content = this.getOrDefault(DataComponents.WRITTEN_BOOK_CONTENT, DEFAULT_CONTENT)
    val updated = WrittenBookContent(Filterable.passThrough(title), content.author, content.generation, content.pages(), content.resolved)
    this.set(DataComponents.WRITTEN_BOOK_CONTENT, updated)
    return this
}

public fun ItemStack.signBook(): ItemStack {
    return if (this.isOf(Items.WRITTEN_BOOK)) this else this.transmuteCopy(Items.WRITTEN_BOOK)
}

public fun ItemStack.unsignBook(): ItemStack {
    return if (this.isOf(Items.WRITABLE_BOOK)) this else this.transmuteCopy(Items.WRITABLE_BOOK)
}

private fun compactToPage(lines: Array<out Component>): Component {
    return when {
        lines.isEmpty() -> CommonComponents.EMPTY
        lines.size == 1 -> lines[0]
        else -> lines.toList().join(Component.literal("\n"))
    }
}