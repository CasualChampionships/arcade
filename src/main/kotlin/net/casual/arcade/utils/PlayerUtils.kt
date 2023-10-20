package net.casual.arcade.utils

import net.casual.arcade.Arcade
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.utils.ExtensionUtils.addExtension
import net.casual.arcade.utils.ExtensionUtils.getExtension
import net.casual.arcade.utils.ExtensionUtils.getExtensions
import net.casual.arcade.utils.TeamUtils.asPlayerTeam
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.utils.impl.Sound
import net.minecraft.advancements.Advancement
import net.minecraft.core.Direction8
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.OutgoingChatMessage
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.network.protocol.game.*
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.Action.ADD
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import java.util.function.Consumer

public object PlayerUtils {
    @JvmStatic
    public val ServerPlayer.location: Location
        get() = Location(this.serverLevel(), Vec3(this.x, this.y, this.z), Vec2(this.xRot, this.yRot))

    @JvmStatic
    public val ServerPlayer.isSurvival: Boolean
        get() = this.isGameMode(GameType.SURVIVAL)

    @JvmStatic
    public fun players(): Collection<ServerPlayer> {
        return Arcade.getServer().playerList.players
    }

    @JvmStatic
    public fun forEveryPlayer(consumer: Consumer<ServerPlayer>) {
        for (player in this.players()) {
            consumer.accept(player)
        }
    }

    @JvmStatic
    public fun broadcast(message: Component) {
        for (player in this.players()) {
            player.sendSystemMessage(message)
        }
    }

    @JvmStatic
    public fun broadcastToOps(message: Component) {
        for (player in this.players()) {
            if (player.hasPermissions(2)) {
                player.sendSystemMessage(message)
            }
        }
        Arcade.getServer().sendSystemMessage(message)
    }

    @JvmStatic
    public fun spread(
        level: ServerLevel,
        center: Vec2,
        distance: Double,
        range: Double,
        teams: Boolean,
        targets: Collection<ServerPlayer>
    ) {
        SpreadPlayers.run(level, center, distance, range, level.maxBuildHeight, teams, targets)
    }

    @JvmStatic
    public fun player(name: String): ServerPlayer? {
        return Arcade.getServer().playerList.getPlayerByName(name)
    }

    @JvmStatic
    public fun ServerPlayer.clearPlayerInventory() {
        this.inventory.clearContent()
        this.inventoryMenu.clearCraftingContent()
        this.inventoryMenu.carried = ItemStack.EMPTY
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
    public fun ServerPlayer.grantAdvancement(advancement: Advancement) {
        val progress = this.advancements.getOrStartProgress(advancement)
        if (!progress.isDone) {
            for (string in progress.remainingCriteria) {
                this.advancements.award(advancement, string)
            }
        }
    }

    @JvmStatic
    public fun ServerPlayer.revokeAdvancement(advancement: Advancement) {
        val progress = this.advancements.getOrStartProgress(advancement)
        if (progress.hasProgress()) {
            for (string in progress.completedCriteria) {
                this.advancements.revoke(advancement, string)
            }
        }
    }

    @JvmStatic
    public fun ServerPlayer.teleportTo(location: Location) {
        this.teleportTo(location.level, location.x, location.y, location.z, location.yaw, location.pitch)
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
    public fun ServerPlayer.sendSound(sound: Sound) {
        this.sendSound(sound.sound, sound.source, sound.volume, sound.pitch)
    }

    @JvmStatic
    @JvmOverloads
    public fun ServerPlayer.sendSound(
        sound: SoundEvent,
        source: SoundSource = SoundSource.MASTER,
        volume: Float = 1.0F,
        pitch: Float = 1.0F
    ) {
        this.playNotifySound(sound, source, volume, pitch)
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
    public fun ServerPlayer.message(message: Component) {
        this.message(PlayerChatMessage.unsigned(this.uuid, message.string).withUnsignedContent(message))
    }

    @JvmStatic
    public fun ServerPlayer.message(message: PlayerChatMessage) {
        this.server.playerList.broadcastChatMessage(message, this, ChatType.bind(ChatType.CHAT, this))
    }

    @JvmStatic
    public fun ServerPlayer.teamMessage(message: Component): Boolean {
        return this.teamMessage(PlayerChatMessage.unsigned(this.uuid, message.string).withUnsignedContent(message))
    }

    @JvmStatic
    public fun ServerPlayer.teamMessage(message: PlayerChatMessage): Boolean {
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
        count: Int = 1,
        alwaysRender: Boolean = false
    ) {
        this.connection.send(ClientboundLevelParticlesPacket(
            options, alwaysRender, position.x, position.y, position.z, xDist, yDist, zDist, speed, count
        ))
    }

    @JvmStatic
    public fun ServerPlayer.addExtension(extension: Extension) {
        (this as ExtensionHolder).addExtension(extension)
    }

    @JvmStatic
    public fun <T: Extension> ServerPlayer.getExtension(type: Class<T>): T {
        return (this as ExtensionHolder).getExtension(type)
    }

    @JvmStatic
    public fun ServerPlayer.getExtensions(): Collection<Extension> {
        return (this as ExtensionHolder).getExtensions()
    }
}