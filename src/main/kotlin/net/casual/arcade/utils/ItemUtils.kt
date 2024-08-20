package net.casual.arcade.utils

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import net.casual.arcade.utils.ComponentUtils.literal
import net.minecraft.ChatFormatting
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Unit
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.block.LightBlock
import java.util.*

public object ItemUtils {
    @JvmStatic
    public fun Item.named(text: Component): ItemStack {
        return ItemStack(this).named(text)
    }

    @JvmStatic
    public fun Item.named(name: String): ItemStack {
        return ItemStack(this).named(name)
    }

    @JvmStatic
    public fun ItemStack.named(text: Component): ItemStack {
        this.set(DataComponents.ITEM_NAME, text)
        return this
    }

    @JvmStatic
    public fun ItemStack.named(name: String): ItemStack {
        this.set(DataComponents.ITEM_NAME, name.literal())
        return this
    }

    @JvmStatic
    public fun ItemStack.isOf(item: Item): Boolean {
        return this.`is`(item)
    }

    @JvmStatic
    public fun ItemStack.styledLore(vararg lore: Component): ItemStack {
        this.set(DataComponents.LORE, ItemLore(lore.toList()))
        return this
    }

    @JvmStatic
    public fun ItemStack.styledLore(lore: List<Component>): ItemStack {
        this.set(DataComponents.LORE, ItemLore(lore))
        return this
    }

    @JvmStatic
    public fun ItemStack.lore(vararg lore: Component): ItemStack {
        val list = lore.toList()
        this.set(DataComponents.LORE, ItemLore(list, list))
        return this
    }

    @JvmStatic
    public fun ItemStack.lore(lore: List<Component>): ItemStack {
        this.set(DataComponents.LORE, ItemLore(lore, lore))
        return this
    }

    @JvmStatic
    public fun ItemStack.hideTooltip(): ItemStack {
        this.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE)
        return this
    }

    @JvmStatic
    public fun ItemStack.hideAttributeTooltips(): ItemStack {
        val modifiers = this.get(DataComponents.ATTRIBUTE_MODIFIERS)
        if (modifiers != null && modifiers.showInTooltip) {
            this.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers(modifiers.modifiers, false))
        }
        return this
    }

    @JvmStatic
    public fun ItemStack.hideTrimTooltips(): ItemStack {
        val trim = this.get(DataComponents.TRIM)
        if (trim != null) {
            this.set(DataComponents.TRIM, trim.withTooltip(false))
        }
        return this
    }

    @JvmStatic
    @Deprecated("Use DataComponents")
    public fun ItemStack.putElement(key: String, tag: Tag): ItemStack {
        CustomData.update(DataComponents.CUSTOM_DATA, this) { compound ->
            compound.put(key, tag)
        }
        return this
    }

    @JvmStatic
    @Deprecated("Use DataComponents")
    public fun ItemStack.putIntElement(key: String, int: Int): ItemStack {
        return this.putElement(key, IntTag.valueOf(int))
    }

    @JvmStatic
    @Deprecated("Use DataComponents")
    public fun ItemStack.putByteElement(key: String, byte: Byte): ItemStack {
        return this.putElement(key, ByteTag.valueOf(byte))
    }

    @JvmStatic
    @Deprecated("Use DataComponents")
    public fun ItemStack.putShortElement(key: String, short: Short): ItemStack {
        return this.putElement(key, ShortTag.valueOf(short))
    }

    @JvmStatic
    @Deprecated("Use DataComponents")
    public fun ItemStack.putLongElement(key: String, long: Long): ItemStack {
        return this.putElement(key, LongTag.valueOf(long))
    }

    @JvmStatic
    @Deprecated("Use DataComponents")
    public fun ItemStack.putFloatElement(key: String, float: Float): ItemStack {
        return this.putElement(key, FloatTag.valueOf(float))
    }

    @JvmStatic
    @Deprecated("Use DataComponents")
    public fun ItemStack.putDoubleElement(key: String, double: Double): ItemStack {
        return this.putElement(key, DoubleTag.valueOf(double))
    }

    @JvmStatic
    @Deprecated("Use DataComponents")
    public fun ItemStack.putStringElement(key: String, string: String): ItemStack {
        return this.putElement(key, StringTag.valueOf(string))
    }

    @JvmStatic
    public fun light(level: Int): ItemStack {
        return LightBlock.setLightOnStack(ItemStack(Items.LIGHT), level)
    }

    @JvmStatic
    public fun ItemStack.potion(potion: Holder<Potion>): ItemStack {
        this.set(DataComponents.POTION_CONTENTS, PotionContents(potion))
        return this
    }

    @JvmStatic
    public fun ItemStack.hasGlint(): Boolean {
        return this.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE) ?: false
    }

    @JvmStatic
    public fun ItemStack.enableGlint(): ItemStack {
        this.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
        return this
    }

    @JvmStatic
    public fun ItemStack.disableGlint(): ItemStack {
        this.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false)
        return this
    }

    @JvmStatic
    public fun ItemStack.addEnchantment(enchantment: Holder<Enchantment>, level: Int): ItemStack {
        var stack = this
        if (this.isOf(Items.BOOK)) {
            val copy = this.transmuteCopy(Items.ENCHANTED_BOOK, this.count)
            copy.set(DataComponents.STORED_ENCHANTMENTS, this.remove(DataComponents.ENCHANTMENTS))
            stack = copy
        }

        EnchantmentHelper.updateEnchantments(stack) { mutable ->
            mutable.set(enchantment, level)
        }
        return stack
    }

    @JvmStatic
    public fun ItemStack.removeEnchantments(): ItemStack {
        EnchantmentHelper.updateEnchantments(this) { mutable ->
            mutable.keySet().clear()
        }
        return this
    }

    @JvmStatic
    public fun createTexturedHead(texture: String): ItemStack {
        val stack = ItemStack(Items.PLAYER_HEAD)
        val properties = PropertyMap()
        properties.put("textures", Property("textures", texture))
        val profile = ResolvableProfile(Optional.empty(), Optional.empty(), properties)

        stack.set(DataComponents.PROFILE, profile)
        return stack
    }

    @JvmStatic
    @JvmOverloads
    public fun createPlayerHead(name: String, item: Item = Items.PLAYER_HEAD): ItemStack {
        val stack = ItemStack(item)
        val profile = ResolvableProfile(Optional.of(name), Optional.empty(), PropertyMap())
        stack.set(DataComponents.PROFILE, profile)
        return stack
    }

    @JvmStatic
    @JvmOverloads
    public fun createPlayerHead(uuid: UUID, item: Item = Items.PLAYER_HEAD): ItemStack {
        val stack = ItemStack(item)
        val profile = ResolvableProfile(Optional.empty(), Optional.of(uuid), PropertyMap())
        stack.set(DataComponents.PROFILE, profile)
        return stack
    }

    @JvmStatic
    @JvmOverloads
    public fun createPlayerHead(profile: GameProfile, item: Item = Items.PLAYER_HEAD): ItemStack {
        val stack = ItemStack(item)
        stack.set(DataComponents.PROFILE, ResolvableProfile(profile))
        return stack
    }

    @JvmStatic
    @JvmOverloads
    public fun createPlayerHead(player: ServerPlayer, item: Item = Items.PLAYER_HEAD): ItemStack {
        return this.createPlayerHead(player.gameProfile, item)
    }

    @JvmStatic
    public fun colouredHeadForFormatting(formatting: ChatFormatting): ItemStack {
        val texture = when (formatting) {
            ChatFormatting.BLACK -> HeadTextures.BLACK
            ChatFormatting.DARK_BLUE -> HeadTextures.DARK_BLUE
            ChatFormatting.DARK_GREEN -> HeadTextures.DARK_GREEN
            ChatFormatting.DARK_AQUA -> HeadTextures.DARK_AQUA
            ChatFormatting.DARK_RED -> HeadTextures.DARK_RED
            ChatFormatting.DARK_PURPLE -> HeadTextures.DARK_PURPLE
            ChatFormatting.GOLD -> HeadTextures.GOLD
            ChatFormatting.GRAY -> HeadTextures.GRAY
            ChatFormatting.DARK_GRAY -> HeadTextures.DARK_GRAY
            ChatFormatting.BLUE -> HeadTextures.BLUE
            ChatFormatting.GREEN -> HeadTextures.GREEN
            ChatFormatting.AQUA -> HeadTextures.AQUA
            ChatFormatting.RED -> HeadTextures.RED
            ChatFormatting.LIGHT_PURPLE -> HeadTextures.LIGHT_PURPLE
            ChatFormatting.YELLOW -> HeadTextures.YELLOW
            else -> HeadTextures.WHITE
        }
        val stack = createTexturedHead(texture)
        stack.set(DataComponents.ITEM_NAME, formatting.getName().literal())
        return stack
    }
}