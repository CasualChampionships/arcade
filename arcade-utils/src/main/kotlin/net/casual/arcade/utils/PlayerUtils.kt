/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.casual.arcade.util.ducks.ConnectionFaultHolder
import net.casual.arcade.util.ducks.SilentRecipeSender
import net.casual.arcade.util.mixins.PlayerAdvancementsAccessor
import net.casual.arcade.utils.TeamUtils.asPlayerTeam
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction8
import net.minecraft.core.Holder
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.*
import net.minecraft.network.protocol.game.*
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.Action.ADD
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import java.util.*
import java.util.function.Predicate

public object PlayerUtils {
    private val HEALTH_BOOST = ResourceUtils.arcade("health_boost")

    @JvmStatic
    public val ServerPlayer.isSurvival: Boolean
        get() = this.isGameMode(GameType.SURVIVAL)

    @JvmStatic
    @JvmOverloads
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
    public fun Iterable<ServerPlayer>.broadcastToOps(message: Component, level: Int = 2) {
        this.broadcast(message) { it.hasPermissions(level) }
    }

    @JvmStatic
    public fun Iterable<ServerPlayer>.toComponent(): MutableComponent {
        val component = Component.empty()
        for (player in this) {
            if (component.siblings.isNotEmpty()) {
                component.append(", ")
            }
            component.append(player.displayName!!)
        }
        return component
    }

    @JvmStatic
    @JvmOverloads
    public fun Iterable<ServerPlayer>.ops(level: Int = 2): List<ServerPlayer> {
        return this.filter { it.hasPermissions(level) }
    }

    @JvmStatic
    @JvmOverloads
    public fun Iterable<ServerPlayer>.gamemode(type: GameType = GameType.SURVIVAL): List<ServerPlayer> {
        return this.filter { it.isGameMode(type) }
    }

    @JvmStatic
    public fun ServerPlayer.getKillCreditWith(source: DamageSource): Entity? {
        return this.killCredit ?: source.entity
    }

    @JvmStatic
    public fun MinecraftServer.player(name: String): ServerPlayer? {
        return this.playerList.getPlayerByName(name)
    }

    @JvmStatic
    public fun MinecraftServer.player(uuid: UUID): ServerPlayer? {
        return this.playerList.getPlayer(uuid)
    }

    @JvmStatic
    public fun ServerPlayer.setRespawnLocation(
        location: LocationWithLevel<*>,
        force: Boolean = true,
        notify: Boolean = false
    ) {
        val config = ServerPlayer.RespawnConfig(
            location.level.dimension(),
            BlockPos.containing(location.position),
            location.yRot,
            force
        )
        this.setRespawnPosition(config, notify)
    }

    @JvmStatic
    public fun ServerPlayer.resetHealth() {
        this.health = this.maxHealth
    }

    @JvmStatic
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
    public fun ServerPlayer.unboostHealth() {
        this.attributes.getInstance(Attributes.MAX_HEALTH)?.removeModifier(HEALTH_BOOST)
    }

    @JvmStatic
    public fun ServerPlayer.resetExperience() {
        this.experienceLevel = 0
        this.experienceProgress = 0.0F
    }

    @JvmStatic
    public fun ServerPlayer.resetHunger() {
        this.foodData.foodLevel = 20
        this.foodData.setSaturation(20.0F)
    }

    @JvmStatic
    public fun ServerPlayer.clearPlayerInventory() {
        this.inventory.clearContent()
        this.inventoryMenu.craftSlots.clearContent()
        this.inventoryMenu.carried = ItemStack.EMPTY
    }

    @JvmStatic
    public fun ServerPlayer.updateSelectedSlot() {
        val menu = this.inventoryMenu
        val slot = this.inventory.selectedSlot + 36
        val item = menu.getSlot(slot).item
        val update = ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), slot, item)
        this.connection.send(update)
    }

    @JvmStatic
    public fun ServerPlayer.isGameMode(mode: GameType): Boolean {
        return this.gameMode.gameModeForPlayer == mode
    }

    @JvmStatic
    public fun ServerPlayer.spoofTeam(team: PlayerTeam) {
        this.server.playerList.broadcastAll(
            ClientboundSetPlayerTeamPacket.createPlayerPacket(team, this.scoreboardName, ADD)
        )
    }

    @JvmStatic
    public fun ServerCommonPacketListenerImpl.hasTimedOut(): Boolean {
        return (this as ConnectionFaultHolder).`arcade$hasTimedOut`()
    }

    @JvmStatic
    public fun ServerPlayer.hasTimedOut(): Boolean {
        return this.connection.hasTimedOut()
    }

    @JvmStatic
    public fun ServerCommonPacketListenerImpl.getPacketError(): Throwable? {
        return (this as ConnectionFaultHolder).`arcade$getPacketError`()
    }

    @JvmStatic
    public fun ServerPlayer.getPacketError(): Throwable? {
        return this.connection.getPacketError()
    }

    @JvmStatic
    public fun ServerPlayer.hasAdvancement(advancement: AdvancementHolder): Boolean {
        return this.advancements.getOrStartProgress(advancement).isDone
    }

    @JvmStatic
    public fun ServerPlayer.grantAdvancement(advancement: AdvancementHolder) {
        val progress = this.advancements.getOrStartProgress(advancement)
        if (!progress.isDone) {
            for (string in progress.remainingCriteria) {
                this.advancements.award(advancement, string)
            }
        }
    }

    @JvmStatic
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
    public fun ServerPlayer.revokeAllAdvancements() {
        for (advancement in this.server.advancements.allAdvancements) {
            this.revokeAdvancement(advancement)
        }
    }

    @JvmStatic
    public fun ServerPlayer.grantAllRecipesSilently() {
        for (recipe in this.server.recipeManager.recipes) {
            this.recipeBook.add(recipe.id)
        }
        this.markSilentRecipesDirty()
    }

    @JvmStatic
    public fun ServerPlayer.revokeAllRecipes() {
        this.resetRecipes(this.server.recipeManager.recipes)
    }

    @JvmStatic
    public fun ServerPlayer.markSilentRecipesDirty() {
        (this as SilentRecipeSender).`arcade$markSilentRecipesDirty`()
    }

    @JvmStatic
    public fun ServerPlayer.setTitleAnimation(
        fadeIn: MinecraftTimeDuration = 10.Ticks,
        stay: MinecraftTimeDuration = 70.Ticks,
        fadeOut: MinecraftTimeDuration = 20.Ticks
    ) {
        this.setTitleAnimation(fadeIn.ticks, stay.ticks, fadeOut.ticks)
    }

    @JvmStatic
    public fun ServerPlayer.setTitleAnimation(fadeIn: Int, stay: Int, fadeOut: Int) {
        this.connection.send(ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut))
    }

    @JvmStatic
    @JvmOverloads
    public fun ServerPlayer.clearTitle(resetTimes: Boolean = true) {
        this.connection.send(ClientboundClearTitlesPacket(resetTimes))
    }

    @JvmStatic
    public fun ServerPlayer.sendTitle(title: Component, subtitle: Component? = null) {
        this.connection.send(ClientboundSetTitleTextPacket(title))
        if (subtitle != null) {
            this.connection.send(ClientboundSetSubtitleTextPacket(subtitle))
        }
    }

    @JvmStatic
    @JvmOverloads
    public fun ServerPlayer.sendSubtitle(subtitle: Component, force: Boolean = false) {
        if (force) {
            this.sendTitle(Component.empty(), subtitle)
            return
        }
        this.connection.send(ClientboundSetSubtitleTextPacket(subtitle))
    }

    @JvmStatic
    @JvmOverloads
    public fun ServerPlayer.sendSound(sound: Sound, position: Vec3 = this.position()) {
        this.sendSound(sound.event, sound.source, position, sound.volume, sound.pitch, sound.static)
    }

    @JvmStatic
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

    @JvmStatic
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

    @JvmStatic
    @JvmOverloads
    public fun ServerPlayer.stopSound(sound: SoundEvent, source: SoundSource? = null) {
        this.connection.send(ClientboundStopSoundPacket(sound.location, source))
    }

    @JvmStatic
    @JvmOverloads
    public fun ServerPlayer.stopAllSounds(source: SoundSource? = null) {
        this.connection.send(ClientboundStopSoundPacket(null, source))
    }

    @JvmStatic
    @JvmOverloads
    public fun ServerPlayer.broadcastMessageAsSystem(
        message: Component,
        filter: Predicate<ServerPlayer> = Predicate { true },
        prefix: Component = this.getChatPrefix()
    ) {
        val decorated = Component.empty().append(prefix).append(message)
        for (player in this.server.playerList.players) {
            if (filter.test(player)) {
                player.sendSystemMessage(decorated)
            }
        }
        this.server.sendSystemMessage(decorated)
    }

    @JvmStatic
    public fun ServerPlayer.getChatPrefix(withTeam: Boolean = true): MutableComponent {
        val team = this.team
        if (!withTeam || team == null) {
            return Component.literal("<").append(this.name).append("> ")
        }
        val name = Component.empty().append(team.playerPrefix).append(this.name).append(team.playerSuffix)
        return Component.literal("<").append(name).append("> ")
    }

    @JvmStatic
    public fun ServerPlayer.broadcastUnsignedMessage(message: Component) {
        this.broadcastUnsignedMessage(PlayerChatMessage.unsigned(this.uuid, message.string).withUnsignedContent(message))
    }

    @JvmStatic
    public fun ServerPlayer.broadcastUnsignedMessage(message: PlayerChatMessage) {
        this.server.playerList.broadcastChatMessage(message, this, ChatType.bind(ChatType.CHAT, this))
    }

    @JvmStatic
    public fun ServerPlayer.broadcastUnsignedTeamMessage(message: Component): Boolean {
        return this.broadcastUnsignedTeamMessage(PlayerChatMessage.unsigned(this.uuid, message.string).withUnsignedContent(message))
    }

    @JvmStatic
    public fun ServerPlayer.broadcastUnsignedTeamMessage(message: PlayerChatMessage): Boolean {
        val team = this.team ?: return false

        val teamDisplay = team.asPlayerTeam().displayName
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
    public fun ServerPlayer.addToTeam(team: PlayerTeam) {
        this.server.scoreboard.addPlayerToTeam(this.scoreboardName, team)
    }

    @JvmStatic
    public fun ServerPlayer.removeFromTeam() {
        this.server.scoreboard.removePlayerFromTeam(this.scoreboardName)
    }

    @JvmStatic
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
    public fun ServerPlayer.directionToNearestBorder(): Direction8 {
        val direction = this.directionVectorToNearestBorder()
        return if (direction.x < 0) {
            if (direction.z < 0) {
                Direction8.NORTH_WEST
            } else if (direction.z > 0) {
                Direction8.SOUTH_WEST
            } else {
                Direction8.WEST
            }
        } else if (direction.x > 0) {
            if (direction.z < 0) {
                Direction8.NORTH_EAST
            } else if (direction.z > 0) {
                Direction8.SOUTH_EAST
            } else {
                Direction8.EAST
            }
        } else {
            if (direction.z < 0) {
                Direction8.NORTH
            } else {
                Direction8.SOUTH
            }
        }
    }

    @JvmStatic
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
            options, overrideLimiter, alwaysRender, position.x, position.y, position.z, xDist, yDist, zDist, speed, count
        ))
    }
}