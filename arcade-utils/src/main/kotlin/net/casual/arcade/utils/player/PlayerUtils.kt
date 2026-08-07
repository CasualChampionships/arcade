/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.player

import net.casual.arcade.util.ducks.ConnectionFaultHolder
import net.casual.arcade.util.ducks.SilentRecipeSender
import net.casual.arcade.util.mixins.PlayerAdvancementsAccessor
import net.casual.arcade.utils.ClientboundLevelParticlesPacket
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.chat.PlayerFormattedChat
import net.casual.arcade.utils.compat.SguiCompatLayer
import net.casual.arcade.utils.component.joinToComponent
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.level.server
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.scoreboard.add
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.core.Holder
import net.minecraft.core.SectionPos
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.Action.ADD
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ChunkTrackingView
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.Permission.HasCommandLevel
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.GameType
import net.minecraft.world.level.storage.LevelData
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import java.util.function.Predicate

public val ServerPlayer.server: MinecraftServer
    get() = this.level().server()

public val ServerPlayer.isSurvival: Boolean
    get() = this.isGameMode(GameType.SURVIVAL)

public val ServerPlayer.username: String
    get() = this.gameProfile.name

public fun ServerPlayer.displayName(): Component {
    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    return this.displayName!!
}

public fun ServerPlayer.getGameMode(): GameType {
    return this.gameMode.gameModeForPlayer
}

public fun ServerPlayer.hasPermission(level: PermissionLevel): Boolean {
    return this.hasPermission(HasCommandLevel(level))
}

public fun ServerPlayer.hasPermission(permission: Permission): Boolean {
    return this.permissions().hasPermission(permission)
}

public fun Iterable<ServerPlayer>.broadcast(packet: Packet<*>) {
    for (player in this) {
        player.connection.send(packet)
    }
}

public fun Iterable<ServerPlayer>.broadcast(message: Component) {
    for (player in this) {
        player.sendSystemMessage(message)
    }
}

public fun ServerPlayer.kick(message: Component = Component.literal("You've been kicked")) {
    this.connection.disconnect(message)
}

public fun Iterable<ServerPlayer>.ops(
    permission: Permission = HasCommandLevel(PermissionLevel.GAMEMASTERS)
): List<ServerPlayer> {
    return this.filter { it.hasPermission(permission) }
}

public fun Iterable<ServerPlayer>.toComponent(): MutableComponent {
    return this.joinToComponent { player -> player.displayName() }
}

public fun ServerPlayer.getKillCreditWith(source: DamageSource): Entity? {
    return this.killCredit ?: source.entity
}

public fun ServerPlayer.setRespawnLocation(
    location: LocationWithLevel<*>,
    force: Boolean = true,
    notify: Boolean = false
) {
    val data = LevelData.RespawnData(
        GlobalPos(location.level.dimension(), BlockPos.containing(location.position)),
        location.yRot,
        location.xRot
    )
    this.setRespawnPosition(ServerPlayer.RespawnConfig(data, force), notify)
}

public fun ServerPlayer.resetHealth() {
    this.health = this.maxHealth
}

public fun ServerPlayer.resetExperience() {
    this.experienceLevel = 0
    this.experienceProgress = 0.0F
}

public fun ServerPlayer.resetHunger() {
    this.foodData.foodLevel = 20
    this.foodData.setSaturation(20.0F)
}

public fun ServerPlayer.clearPlayerInventory() {
    this.inventory.clearContent()
    this.inventoryMenu.craftSlots.clearContent()
    this.inventoryMenu.carried = ItemStack.EMPTY
}

public fun ServerPlayer.updateSelectedSlot() {
    this.updateInventorySlot(this.inventory.selectedSlot + 36)
}

public fun ServerPlayer.updateOffhandSlot() {
    this.updateInventorySlot(InventoryMenu.SHIELD_SLOT)
}

public fun ServerPlayer.updateInteractionSlot(hand: InteractionHand) {
    when (hand) {
        InteractionHand.MAIN_HAND -> this.updateSelectedSlot()
        InteractionHand.OFF_HAND -> this.updateOffhandSlot()
    }
}

public fun ServerPlayer.updateInventorySlot(slot: Int) {
    if (!SguiCompatLayer.isInGuiWithOverriddenInventory(this)) {
        val menu = this.inventoryMenu
        val item = menu.getSlot(slot).item
        val update = ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), slot, item)
        this.connection.send(update)
    }
}

public fun ServerPlayer.getAttackCooldown(): MinecraftTimeDuration {
    return Mth.ceil(this.currentItemAttackStrengthDelay).Ticks
}

public fun ServerPlayer.getApproximateViewBox(): AABB {
    val pos = SectionPos.of(this.position())
    val size = (2 * this.getViewDistance() + 1) * 16.0
    return AABB.ofSize(Vec3.atLowerCornerOf(pos.center()), size, size, size)
}

public fun ServerPlayer.getViewDistance(): Int {
    return this.requestedViewDistance().coerceIn(2, this.server.playerList.viewDistance)
}

public fun ServerPlayer.isInViewDistance(pos: Vec3): Boolean {
    return this.isInViewDistance(pos.x, pos.z)
}

public fun ServerPlayer.isInViewDistance(x: Double, z: Double): Boolean {
    return this.isChunkInViewDistance(SectionPos.posToSectionCoord(x), SectionPos.posToSectionCoord(z))
}

public fun ServerPlayer.isChunkInViewDistance(pos: ChunkPos, offset: Int = 0): Boolean {
    return this.isChunkInViewDistance(pos.x, pos.z, offset)
}

public fun ServerPlayer.isChunkInViewDistance(chunkX: Int, chunkY: Int, offset: Int = 0): Boolean {
    val pos = this.chunkPosition()
    return ChunkTrackingView.isInViewDistance(pos.x, pos.z, this.getViewDistance() + offset, chunkX, chunkY)
}

public fun ServerPlayer.dropItemStackIntoInventory(
    stack: ItemStack,
    remaining: (ItemStack) -> Unit
) {
    val count = stack.count
    val item = stack.item
    this.inventory.add(stack)
    if (stack.count < count) {
        val level = this.level()
        level.playSound(
            null, this.x, this.y, this.z,
            SoundEvents.ITEM_PICKUP,
            SoundSource.PLAYERS,
            0.2F,
            (level.random.nextFloat() - level.random.nextFloat()) * 1.4F + 2.0F
        )
        this.awardStat(Stats.ITEM_PICKED_UP.get(item), count - stack.count)
        if (!stack.isEmpty) {
            remaining.invoke(stack)
        }
    } else {
        remaining.invoke(stack)
    }
}

public fun ServerPlayer.isGameMode(mode: GameType): Boolean {
    return this.gameMode.gameModeForPlayer == mode
}

public fun ServerPlayer.spoofTeam(team: PlayerTeam) {
    this.server.playerList.broadcastAll(
        ClientboundSetPlayerTeamPacket.createPlayerPacket(team, this.scoreboardName, ADD)
    )
}

public fun ServerCommonPacketListenerImpl.hasTimedOut(): Boolean {
    return (this as ConnectionFaultHolder).arcade_hasTimeOut()
}

public fun ServerPlayer.hasTimedOut(): Boolean {
    return this.connection.hasTimedOut()
}

public fun ServerCommonPacketListenerImpl.getPacketError(): Throwable? {
    return (this as ConnectionFaultHolder).arcade_getPacketError()
}

public fun ServerPlayer.getPacketError(): Throwable? {
    return this.connection.getPacketError()
}

public fun ServerPlayer.hasAdvancement(advancement: AdvancementHolder): Boolean {
    return this.advancements.getOrStartProgress(advancement).isDone
}

public fun ServerPlayer.grantAdvancement(advancement: AdvancementHolder) {
    val progress = this.advancements.getOrStartProgress(advancement)
    if (!progress.isDone) {
        for (string in progress.remainingCriteria) {
            this.advancements.award(advancement, string)
        }
    }
}

public fun ServerPlayer.grantAdvancementSilently(advancement: AdvancementHolder) {
    val progress = this.advancements.getOrStartProgress(advancement)
    val accessor = this.advancements as PlayerAdvancementsAccessor
    if (!progress.isDone) {
        for (string in progress.remainingCriteria) {
            progress.grantProgress(string)
        }
        accessor.arcade_getProgressChanged().add(advancement)
        accessor.arcade_updateVisibility(advancement)
    }
}

public fun ServerPlayer.revokeAdvancement(advancement: AdvancementHolder) {
    val progress = this.advancements.getOrStartProgress(advancement)
    if (progress.hasProgress()) {
        for (string in progress.completedCriteria) {
            this.advancements.revoke(advancement, string)
        }
    }
    (this.advancements as PlayerAdvancementsAccessor).arcade_getProgress().remove(advancement)
}

public fun ServerPlayer.revokeAllAdvancements() {
    for (advancement in this.server.advancements.allAdvancements) {
        this.revokeAdvancement(advancement)
    }
}

public fun ServerPlayer.grantAllRecipesSilently() {
    for (recipe in this.server.recipeManager.recipes) {
        this.recipeBook.add(recipe.id)
    }
    this.markSilentRecipesDirty()
}

public fun ServerPlayer.revokeAllRecipes() {
    this.resetRecipes(this.server.recipeManager.recipes)
}

public fun ServerPlayer.markSilentRecipesDirty() {
    (this as SilentRecipeSender).arcade_markSilentRecipesDirty()
}

public fun ServerPlayer.setTitleAnimation(
    fadeIn: MinecraftTimeDuration = 10.Ticks,
    stay: MinecraftTimeDuration = 70.Ticks,
    fadeOut: MinecraftTimeDuration = 20.Ticks
) {
    this.setTitleAnimation(fadeIn.ticks, stay.ticks, fadeOut.ticks)
}

public fun ServerPlayer.setTitleAnimation(fadeIn: Int, stay: Int, fadeOut: Int) {
    this.connection.send(ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut))
}

@JvmOverloads
public fun ServerPlayer.clearTitle(resetTimes: Boolean = true) {
    this.connection.send(ClientboundClearTitlesPacket(resetTimes))
}

public fun ServerPlayer.sendTitle(title: Component, subtitle: Component? = null) {
    this.connection.send(ClientboundSetTitleTextPacket(title))
    if (subtitle != null) {
        this.connection.send(ClientboundSetSubtitleTextPacket(subtitle))
    }
}

@JvmOverloads
public fun ServerPlayer.sendSubtitle(subtitle: Component, force: Boolean = false) {
    if (force) {
        this.sendTitle(Component.empty(), subtitle)
        return
    }
    this.connection.send(ClientboundSetSubtitleTextPacket(subtitle))
}

public fun ServerPlayer.sendActionBarMessage(component: Component) {
    this.connection.send(ClientboundSetActionBarTextPacket(component))
}

@JvmOverloads
public fun ServerPlayer.sendSound(sound: Sound, position: Vec3 = this.position()) {
    this.sendSound(sound.event, sound.source, position, sound.volume, sound.pitch, sound.static)
}

@JvmOverloads
public fun ServerPlayer.sendSound(
    sound: SoundEvent,
    source: SoundSource = SoundSource.MASTER,
    position: Vec3 = this.position(),
    volume: Float = 1.0F,
    pitch: Float = 1.0F,
    static: Boolean = true
) {
    this.sendSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), source, position, volume, pitch, static)
}

@JvmOverloads
public fun ServerPlayer.sendSound(
    sound: Holder<SoundEvent>,
    source: SoundSource = SoundSource.MASTER,
    position: Vec3 = this.position(),
    volume: Float = 1.0F,
    pitch: Float = 1.0F,
    static: Boolean = true
) {
    val packet = if (!static) {
        ClientboundSoundPacket(sound, source, position.x, position.y, position.z, volume, pitch, this.random.nextLong())
    } else {
        ClientboundSoundEntityPacket(sound, source, this, volume, pitch, this.random.nextLong())
    }
    this.connection.send(packet)
}

@JvmOverloads
public fun ServerPlayer.stopSound(sound: SoundEvent, source: SoundSource? = null) {
    this.connection.send(ClientboundStopSoundPacket(sound.location, source))
}

@JvmOverloads
public fun ServerPlayer.stopAllSounds(source: SoundSource? = null) {
    this.connection.send(ClientboundStopSoundPacket(null, source))
}

@JvmOverloads
public fun ServerPlayer.broadcastMessageAsSystem(
    message: Component,
    filter: Predicate<ServerPlayer> = Predicate { true },
    username: Component = this.getChatUsername(),
    prefix: Component = CommonComponents.EMPTY
) {
    val formatted = PlayerFormattedChat(prefix, username, message)
    val decorated = formatted.asComponent { CommonComponents.EMPTY }
    for (player in this.server.playerList.players) {
        if (filter.test(player)) {
            player.sendSystemMessage(decorated)
        }
    }
    this.server.sendSystemMessage(decorated)
}

public fun ServerPlayer.getChatUsername(withTeam: Boolean = true): MutableComponent {
    val team = this.team
    if (!withTeam || team == null) {
        return Component.literal("<").append(this.name).append(">")
    }
    val name = team.getFormattedName(this.name)
    return Component.literal("<").append(name).append(">")
}

public fun ServerPlayer.addToTeam(team: PlayerTeam) {
    team.add(this)
}

public fun ServerPlayer.removeFromTeam() {
    this.server.scoreboard.removePlayerFromTeam(this.scoreboardName)
}

public fun ServerPlayer.sendParticles(
    options: ParticleOptions,
    position: Vec3,
    xDist: Float = 0.0F,
    yDist: Float = 0.0F,
    zDist: Float = 0.0F,
    speed: Float = 0.0F,
    count: Int = 0,
    alwaysRender: Boolean = false,
    overrideLimiter: Boolean = false
) {
    this.connection.send(ClientboundLevelParticlesPacket(
        options, position, xDist, yDist, zDist, speed, count, alwaysRender, overrideLimiter
    ))
}