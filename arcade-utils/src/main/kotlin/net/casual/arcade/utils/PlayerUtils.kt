/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.casual.arcade.util.ducks.ConnectionFaultHolder
import net.casual.arcade.util.ducks.SilentRecipeSender
import net.casual.arcade.util.mixins.PlayerAdvancementsAccessor
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.chat.PlayerFormattedChat
import net.casual.arcade.utils.compat.SguiCompatLayer
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.*
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.*
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
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.GameType
import net.minecraft.world.level.storage.LevelData
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import java.util.*
import java.util.function.Predicate

public object PlayerUtils {
    private val HEALTH_BOOST = IdentifierUtils.arcade("health_boost")

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public val ServerPlayer.server: MinecraftServer
        get() = this.level().server!!

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public val ServerPlayer.isSurvival: Boolean
        get() = this.isGameMode(GameType.SURVIVAL)

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public val ServerPlayer.username: String
        get() = this.gameProfile.name

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.server.ServerUtils")
    public val MinecraftServer.players: List<ServerPlayer>
        get() = this.playerList.players

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.getGameMode(): GameType {
        return this.gameMode.gameModeForPlayer
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.hasPermission(level: PermissionLevel): Boolean {
        return this.hasPermission(HasCommandLevel(level))
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.hasPermission(permission: Permission): Boolean {
        return this.permissions().hasPermission(permission)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun Iterable<ServerPlayer>.broadcast(packet: Packet<*>) {
        for (player in this) {
            player.connection.send(packet)
        }
    }

    @JvmStatic
    @JvmOverloads
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun Iterable<ServerPlayer>.broadcast(
        message: Component,
        filter: Predicate<ServerPlayer> = Predicate { true }
    ) {
        for (player in this) {
            if (filter.test(player)) {
                player.sendSystemMessage(message)
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    @Deprecated("Filter ops then broadcast")
    public fun Iterable<ServerPlayer>.broadcastToOps(
        message: Component,
        permission: Permission = HasCommandLevel(PermissionLevel.GAMEMASTERS)
    ) {
        this.broadcast(message) { it.hasPermission(permission) }
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun Iterable<ServerPlayer>.toComponent(): MutableComponent {
        val component = Component.empty()
        for (player in this) {
            if (component.siblings.isNotEmpty()) {
                component.append(", ")
            }
            component.append(player.displayName)
        }
        return component
    }

    @JvmStatic
    @JvmOverloads
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun Iterable<ServerPlayer>.ops(
        permission: Permission = HasCommandLevel(PermissionLevel.GAMEMASTERS)
    ): List<ServerPlayer> {
        return this.filter { it.hasPermission(permission) }
    }

    @JvmStatic
    @JvmOverloads
    @Deprecated("Filter for gamemode yourself")
    public fun Iterable<ServerPlayer>.gamemode(type: GameType = GameType.SURVIVAL): List<ServerPlayer> {
        return this.filter { it.isGameMode(type) }
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.getKillCreditWith(source: DamageSource): Entity? {
        return this.killCredit ?: source.entity
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.server.ServerUtils")
    public fun MinecraftServer.player(name: String): ServerPlayer? {
        return this.playerList.getPlayerByName(name)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.server.ServerUtils")
    public fun MinecraftServer.player(uuid: UUID): ServerPlayer? {
        return this.playerList.getPlayer(uuid)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
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

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.resetHealth() {
        this.health = this.maxHealth
    }

    @JvmStatic
    @Deprecated("Implement yourself")
    public fun ServerPlayer.boostHealth(multiply: Double) {
        val instance = this.attributes.getInstance(Attributes.MAX_HEALTH)
        if (instance != null) {
            instance.removeModifier(HEALTH_BOOST)
            instance.addPermanentModifier(AttributeModifier(
                HEALTH_BOOST,
                multiply,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ))
        }
    }

    @JvmStatic
    @Deprecated("Implement yourself")
    public fun ServerPlayer.unboostHealth() {
        this.attributes.getInstance(Attributes.MAX_HEALTH)?.removeModifier(HEALTH_BOOST)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.resetExperience() {
        this.experienceLevel = 0
        this.experienceProgress = 0.0F
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.resetHunger() {
        this.foodData.foodLevel = 20
        this.foodData.setSaturation(20.0F)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.clearPlayerInventory() {
        this.inventory.clearContent()
        this.inventoryMenu.craftSlots.clearContent()
        this.inventoryMenu.carried = ItemStack.EMPTY
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.updateSelectedSlot() {
        this.updateInventorySlot(this.inventory.selectedSlot + 36)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.updateOffhandSlot() {
        this.updateInventorySlot(InventoryMenu.SHIELD_SLOT)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.updateInteractionSlot(hand: InteractionHand) {
        when (hand) {
            InteractionHand.MAIN_HAND -> this.updateSelectedSlot()
            InteractionHand.OFF_HAND -> this.updateOffhandSlot()
        }
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.updateInventorySlot(slot: Int) {
        if (!SguiCompatLayer.isInGuiWithOverriddenInventory(this)) {
            val menu = this.inventoryMenu
            val item = menu.getSlot(slot).item
            val update = ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), slot, item)
            this.connection.send(update)
        }
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.getAttackCooldown(): MinecraftTimeDuration {
        return Mth.ceil(this.currentItemAttackStrengthDelay).Ticks
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.getApproximateViewBox(): AABB {
        val pos = SectionPos.of(this.position())
        val size = (2 * this.getViewDistance() + 1) * 16.0
        return AABB.ofSize(Vec3.atLowerCornerOf(pos.center()), size, size, size)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.getViewDistance(): Int {
        return this.requestedViewDistance().coerceIn(2, this.server.playerList.viewDistance)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.isInViewDistance(pos: Vec3): Boolean {
        return this.isInViewDistance(pos.x, pos.z)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.isInViewDistance(x: Double, z: Double): Boolean {
        return this.isChunkInViewDistance(SectionPos.posToSectionCoord(x), SectionPos.posToSectionCoord(z))
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.isChunkInViewDistance(pos: ChunkPos, offset: Int = 0): Boolean {
        return this.isChunkInViewDistance(pos.x, pos.z, offset)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.isChunkInViewDistance(chunkX: Int, chunkY: Int, offset: Int = 0): Boolean {
        val pos = this.chunkPosition()
        return ChunkTrackingView.isInViewDistance(pos.x, pos.z, this.getViewDistance() + offset, chunkX, chunkY)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
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

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.isGameMode(mode: GameType): Boolean {
        return this.gameMode.gameModeForPlayer == mode
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.spoofTeam(team: PlayerTeam) {
        this.server.playerList.broadcastAll(
            ClientboundSetPlayerTeamPacket.createPlayerPacket(team, this.scoreboardName, ADD)
        )
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerCommonPacketListenerImpl.hasTimedOut(): Boolean {
        return (this as ConnectionFaultHolder).arcade_hasTimeOut()
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.hasTimedOut(): Boolean {
        return this.connection.hasTimedOut()
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerCommonPacketListenerImpl.getPacketError(): Throwable? {
        return (this as ConnectionFaultHolder).arcade_getPacketError()
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.getPacketError(): Throwable? {
        return this.connection.getPacketError()
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.hasAdvancement(advancement: AdvancementHolder): Boolean {
        return this.advancements.getOrStartProgress(advancement).isDone
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.grantAdvancement(advancement: AdvancementHolder) {
        val progress = this.advancements.getOrStartProgress(advancement)
        if (!progress.isDone) {
            for (string in progress.remainingCriteria) {
                this.advancements.award(advancement, string)
            }
        }
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.grantAdvancementSilently(advancement: AdvancementHolder) {
        val progress = this.advancements.getOrStartProgress(advancement)
        val accessor = this.advancements as PlayerAdvancementsAccessor
        if (!progress.isDone) {
            for (string in progress.remainingCriteria) {
                progress.grantProgress(string)
            }
            accessor.progressChanged.add(advancement)
            accessor.updateVisibility(advancement)
        }
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.revokeAdvancement(advancement: AdvancementHolder) {
        val progress = this.advancements.getOrStartProgress(advancement)
        if (progress.hasProgress()) {
            for (string in progress.completedCriteria) {
                this.advancements.revoke(advancement, string)
            }
        }
        (this.advancements as PlayerAdvancementsAccessor).progress.remove(advancement)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.revokeAllAdvancements() {
        for (advancement in this.server.advancements.allAdvancements) {
            this.revokeAdvancement(advancement)
        }
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.grantAllRecipesSilently() {
        for (recipe in this.server.recipeManager.recipes) {
            this.recipeBook.add(recipe.id)
        }
        this.markSilentRecipesDirty()
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.revokeAllRecipes() {
        this.resetRecipes(this.server.recipeManager.recipes)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.markSilentRecipesDirty() {
        (this as SilentRecipeSender).`arcade$markSilentRecipesDirty`()
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.setTitleAnimation(
        fadeIn: MinecraftTimeDuration = 10.Ticks,
        stay: MinecraftTimeDuration = 70.Ticks,
        fadeOut: MinecraftTimeDuration = 20.Ticks
    ) {
        this.setTitleAnimation(fadeIn.ticks, stay.ticks, fadeOut.ticks)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.setTitleAnimation(fadeIn: Int, stay: Int, fadeOut: Int) {
        this.connection.send(ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut))
    }

    @JvmStatic
    @JvmOverloads
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.clearTitle(resetTimes: Boolean = true) {
        this.connection.send(ClientboundClearTitlesPacket(resetTimes))
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.sendTitle(title: Component, subtitle: Component? = null) {
        this.connection.send(ClientboundSetTitleTextPacket(title))
        if (subtitle != null) {
            this.connection.send(ClientboundSetSubtitleTextPacket(subtitle))
        }
    }

    @JvmStatic
    @JvmOverloads
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.sendSubtitle(subtitle: Component, force: Boolean = false) {
        if (force) {
            this.sendTitle(Component.empty(), subtitle)
            return
        }
        this.connection.send(ClientboundSetSubtitleTextPacket(subtitle))
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.sendActionBarMessage(component: Component) {
        this.connection.send(ClientboundSetActionBarTextPacket(component))
    }

    @JvmStatic
    @JvmOverloads
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.sendSound(sound: Sound, position: Vec3 = this.position()) {
        this.sendSound(sound.event, sound.source, position, sound.volume, sound.pitch, sound.static)
    }

    @JvmStatic
    @JvmOverloads
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
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

    @JvmStatic
    @JvmOverloads
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
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

    @JvmStatic
    @JvmOverloads
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.stopSound(sound: SoundEvent, source: SoundSource? = null) {
        this.connection.send(ClientboundStopSoundPacket(sound.location, source))
    }

    @JvmStatic
    @JvmOverloads
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.stopAllSounds(source: SoundSource? = null) {
        this.connection.send(ClientboundStopSoundPacket(null, source))
    }

    @JvmStatic
    @JvmOverloads
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
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

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.getChatUsername(withTeam: Boolean = true): MutableComponent {
        val team = this.team
        if (!withTeam || team == null) {
            return Component.literal("<").append(this.name).append(">")
        }
        val name = team.getFormattedName(this.name)
        return Component.literal("<").append(name).append(">")
    }

    @JvmStatic
    @Deprecated("No replacement")
    public fun ServerPlayer.broadcastUnsignedMessage(message: Component) {
        this.broadcastUnsignedMessage(PlayerChatMessage.unsigned(this.uuid, message.string).withUnsignedContent(message))
    }

    @JvmStatic
    @Deprecated("No replacement")
    public fun ServerPlayer.broadcastUnsignedMessage(message: PlayerChatMessage) {
        this.server.playerList.broadcastChatMessage(message, this, ChatType.bind(ChatType.CHAT, this))
    }

    @JvmStatic
    @Deprecated("No replacement")
    public fun ServerPlayer.broadcastUnsignedTeamMessage(message: Component): Boolean {
        return this.broadcastUnsignedTeamMessage(PlayerChatMessage.unsigned(this.uuid, message.string).withUnsignedContent(message))
    }

    @JvmStatic
    @Deprecated("No replacement")
    public fun ServerPlayer.broadcastUnsignedTeamMessage(message: PlayerChatMessage): Boolean {
        val team = this.team ?: return false

        val teamDisplay = team.displayName
        val inbound = ChatType.bind(ChatType.TEAM_MSG_COMMAND_INCOMING, this).withTargetName(teamDisplay)
        val outbound = ChatType.bind(ChatType.TEAM_MSG_COMMAND_OUTGOING, this).withTargetName(teamDisplay)

        val outgoing = OutgoingChatMessage.create(message)

        for (teammates in team.getOnlinePlayers()) {
            val bound = if (teammates === this) outbound else inbound
            val filter = this.shouldFilterMessageTo(teammates)
            teammates.sendChatMessage(outgoing, filter, bound)
        }
        return true
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.addToTeam(team: PlayerTeam) {
        this.server.scoreboard.addPlayerToTeam(this.scoreboardName, team)
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
    public fun ServerPlayer.removeFromTeam() {
        this.server.scoreboard.removePlayerFromTeam(this.scoreboardName)
    }

    @JvmStatic
    @Deprecated("No replacement")
    public fun ServerPlayer.distanceToBorders(): Vec3 {
        val border = this.level().worldBorder
        val distanceToEast = this.x - border.minX
        val distanceToWest = border.maxX - this.x
        val distanceToNorth = this.z - border.minZ
        val distanceToSouth = border.maxZ - this.z
        val distanceToX = distanceToEast.coerceAtMost(distanceToWest)
        val distanceToZ = distanceToNorth.coerceAtMost(distanceToSouth)
        return Vec3(distanceToX, 0.0, distanceToZ)
    }

    @JvmStatic
    @Deprecated("No replacement")
    public fun ServerPlayer.distanceToNearestBorder(): Vec3 {
        val distance = this.distanceToBorders()
        return when {
            distance.x < 0 && distance.z < 0 -> distance
            distance.x < 0 -> Vec3(distance.x, 0.0, 0.0)
            distance.z < 0 -> Vec3(0.0, 0.0, distance.z)
            distance.x < distance.z -> Vec3(distance.x, 0.0, 0.0)
            else -> Vec3(0.0, 0.0, distance.z)
        }
    }

    @JvmStatic
    @Deprecated("No replacement")
    public fun ServerPlayer.directionVectorToBorders(): Vec3 {
        val border = this.level().worldBorder
        val distanceToEast = this.x - border.minX
        val distanceToWest = border.maxX - this.x
        val distanceToNorth = this.z - border.minZ
        val distanceToSouth = border.maxZ - this.z
        val distanceToX = if (distanceToEast < distanceToWest) -distanceToEast else distanceToWest
        val distanceToZ = if (distanceToNorth < distanceToSouth) -distanceToNorth else distanceToSouth
        return Vec3(distanceToX, 0.0, distanceToZ)
    }

    @JvmStatic
    @Deprecated("No replacement")
    public fun ServerPlayer.directionVectorToNearestBorder(): Vec3 {
        val distance = this.distanceToBorders()
        val direction = this.directionVectorToBorders()
        return when {
            distance.x < 0 && distance.z < 0 -> direction
            distance.x < 0 -> Vec3(direction.x, 0.0, 0.0)
            distance.z < 0 -> Vec3(0.0, 0.0, direction.z)
            distance.x < distance.z -> Vec3(direction.x, 0.0, 0.0)
            else -> Vec3(0.0, 0.0, direction.z)
        }
    }

    @JvmStatic
    @Deprecated("No replacement")
    public fun ServerPlayer.directionToNearestBorder(): Direction8 {
        return MathUtils.getDirection8(this.directionVectorToNearestBorder())
    }

    @JvmStatic
    @Deprecated("Use replacement in net.casual.arcade.utils.player.PlayerUtils")
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
}