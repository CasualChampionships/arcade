package net.casual.arcade.utils

import net.casual.arcade.utils.ComponentUtils.unitalicise
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component
import net.minecraft.world.item.*
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.EnchantmentInstance

@Suppress("unused")
object ItemUtils {
    @JvmStatic
    fun Item.literalNamed(name: String): ItemStack {
        return this.defaultInstance.literalNamed(name)
    }

    @JvmStatic
    fun Item.translatableNamed(key: String, vararg args: Any): ItemStack {
        return this.defaultInstance.translatableNamed(key, args)
    }

    @JvmStatic
    fun ItemStack.literalNamed(name: String): ItemStack {
        return this.setHoverName(Component.literal(name))
    }

    @JvmStatic
    fun ItemStack.translatableNamed(key: String, vararg args: Any): ItemStack {
        return this.setHoverName(Component.translatable(key, args))
    }

    @JvmStatic
    fun ItemStack.setLore(lore: Iterable<Component>) {
        val list = ListTag()
        for (component in lore) {
            list.add(StringTag.valueOf(Component.Serializer.toJson(component)))
        }
        this.addTagElement(ItemStack.TAG_LORE, list)
    }

    @JvmStatic
    fun ItemStack.putElement(key: String, tag: Tag): ItemStack {
        this.addTagElement(key, tag)
        return this
    }

    @JvmStatic
    fun ItemStack.putIntElement(key: String, int: Int): ItemStack {
        return this.putElement(key, IntTag.valueOf(int))
    }

    @JvmStatic
    fun ItemStack.putByteElement(key: String, byte: Byte): ItemStack {
        return this.putElement(key, ByteTag.valueOf(byte))
    }

    @JvmStatic
    fun ItemStack.putShortElement(key: String, short: Short): ItemStack {
        return this.putElement(key, ShortTag.valueOf(short))
    }

    @JvmStatic
    fun ItemStack.putLongElement(key: String, long: Long): ItemStack {
        return this.putElement(key, LongTag.valueOf(long))
    }

    @JvmStatic
    fun ItemStack.putFloatElement(key: String, float: Float): ItemStack {
        return this.putElement(key, FloatTag.valueOf(float))
    }

    @JvmStatic
    fun ItemStack.putDoubleElement(key: String, double: Double): ItemStack {
        return this.putElement(key, DoubleTag.valueOf(double))
    }

    @JvmStatic
    fun ItemStack.putStringElement(key: String, string: String): ItemStack {
        return this.putElement(key, StringTag.valueOf(string))
    }

    @JvmStatic
    fun ItemStack.potion(potion: Potion): ItemStack {
        return PotionUtils.setPotion(this, potion)
    }

    @JvmStatic
    fun ItemStack.enableGlint(): ItemStack {
        val tag = this.getOrCreateTag()
        val enchants = ListTag()
        enchants.add(CompoundTag())
        tag.put(ItemStack.TAG_ENCH, enchants)
        return this
    }

    @JvmStatic
    fun ItemStack.addEnchantment(enchantment: Enchantment, level: Int): ItemStack {
        val id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment) ?: return this
        val enchants = this.enchantmentTags
        enchants.add(EnchantmentHelper.storeEnchantment(id, level))
        this.addTagElement(ItemStack.TAG_ENCH, enchants)
        if (this.`is`(Items.ENCHANTED_BOOK)) {
            EnchantedBookItem.addEnchantment(this, EnchantmentInstance(enchantment, level))
        }
        return this
    }

    @JvmStatic
    fun ItemStack.removeEnchantments(): ItemStack {
        this.removeTagKey(ItemStack.TAG_ENCH)
        return this
    }

    @JvmStatic
    fun generatePlayerHead(playerName: String, texture: String? = null): ItemStack {
        val compound = CompoundTag()
        compound.putString("id", "player_head")
        compound.putByte("Count", 1.toByte())
        if (texture != null) {
            val skullData = CompoundTag()
            skullData.putString("Name", playerName)
            val textureCompound = CompoundTag()
            textureCompound.putString("Value", texture)
            val textures = ListTag()
            textures.add(textureCompound)
            val properties = CompoundTag()
            properties.put("textures", textures)
            skullData.put("Properties", properties)
            val playerData = CompoundTag()
            playerData.put(PlayerHeadItem.TAG_SKULL_OWNER, skullData)
            compound.put("tag", playerData)
        } else {
            val playerData = CompoundTag()
            playerData.putString(PlayerHeadItem.TAG_SKULL_OWNER, playerName)
            compound.put("tag", playerData)
        }
        return ItemStack.of(compound)
    }

    @JvmStatic
    fun colouredHeadForFormatting(formatting: ChatFormatting): ItemStack {
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
        val item = generatePlayerHead("Dummy", texture)
        return item.setHoverName(Component.literal(formatting.getName()).unitalicise())
    }
}