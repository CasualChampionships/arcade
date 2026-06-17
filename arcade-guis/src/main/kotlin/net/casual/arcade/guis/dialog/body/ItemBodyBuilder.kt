/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.body

import net.casual.arcade.guis.utils.dialog.ItemBodyDimension
import net.casual.arcade.utils.ItemUtils.template
import net.minecraft.network.chat.Component
import net.minecraft.server.dialog.body.ItemBody
import net.minecraft.server.dialog.body.PlainMessage
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import java.util.Optional

public class ItemBodyBuilder: BodyBuilder() {
    // We need to do this so we can overload item and still call this::template.isInitialized
    private lateinit var _item: ItemStackTemplate

    public var item: ItemStackTemplate by this::_item
    public var description: PlainMessage? = null
    public var showDecorations: Boolean = false
    public var showTooltip: Boolean = false
    public var width: ItemBodyDimension = ItemBodyDimension.DEFAULT
    public var height: ItemBodyDimension = ItemBodyDimension.DEFAULT

    public fun item(item: ItemStackTemplate): ItemBodyBuilder {
        this.item = item
        return this
    }

    public fun item(item: ItemStack): ItemBodyBuilder {
        this.item = item.template()
        return this
    }

    public fun description(description: PlainMessage?): ItemBodyBuilder {
        this.description = description
        return this
    }

    public fun description(contents: Component, width: Int = PlainMessage.DEFAULT_WIDTH): ItemBodyBuilder {
        return this.description(PlainMessage(contents, width))
    }

    public fun showDecorations(showDecorations: Boolean): ItemBodyBuilder {
        this.showDecorations = showDecorations
        return this
    }

    public fun showTooltip(showTooltip: Boolean): ItemBodyBuilder {
        this.showTooltip = showTooltip
        return this
    }

    public fun width(width: Int): ItemBodyBuilder {
        this.width = ItemBodyDimension(width)
        return this
    }

    public fun height(height: Int): ItemBodyBuilder {
        this.height = ItemBodyDimension(height)
        return this
    }

    override fun build(): ItemBody {
        require(this::_item.isInitialized) { "Item wasn't initialized" }
        return ItemBody(
            this.item,
            Optional.ofNullable(this.description),
            this.showDecorations,
            this.showTooltip,
            this.width.value,
            this.height.value
        )
    }
}