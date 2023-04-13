package net.casualuhc.arcade.utils

import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.extensions.Extension
import net.casualuhc.arcade.extensions.ExtensionHolder
import net.casualuhc.arcade.math.Location
import net.casualuhc.arcade.utils.ExtensionUtils.addExtension
import net.casualuhc.arcade.utils.ExtensionUtils.getExtension
import net.casualuhc.arcade.utils.ExtensionUtils.getExtensions
import net.casualuhc.arcade.utils.TeamUtils.asPlayerTeam
import net.casualuhc.arcade.utils.TeamUtils.getServerPlayers
import net.minecraft.advancements.Advancement
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.OutgoingChatMessage
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.util.function.Consumer

@Suppress("unused")
object PlayerUtils {
    @JvmStatic
    val ServerPlayer.location
        get() = Location(this.getLevel(), Vec3(this.x, this.y, this.z), Vec2(this.xRot, this.yRot))

    @JvmStatic
    val ServerPlayer.isSurvival get() = this.isGameMode(GameType.SURVIVAL)

    @JvmStatic
    fun players(): Collection<ServerPlayer> {
        return Arcade.server.playerList.players
    }

    @JvmStatic
    fun forEveryPlayer(consumer: Consumer<ServerPlayer>) {
        for (player in this.players()) {
            consumer.accept(player)
        }
    }

    @JvmStatic
    fun broadcast(message: Component) {
        for (player in this.players()) {
            player.sendSystemMessage(message)
        }
    }

    @JvmStatic
    fun broadcastToOps(message: Component) {
        for (player in this.players()) {
            if (player.hasPermissions(2)) {
                player.sendSystemMessage(message)
            }
        }
        Arcade.server.sendSystemMessage(message)
    }

    @JvmStatic
    fun spread(
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
    fun player(name: String): ServerPlayer? {
        return Arcade.server.playerList.getPlayerByName(name)
    }

    @JvmStatic
    fun ServerPlayer.clearPlayerInventory() {
        this.inventory.clearContent()
        this.inventoryMenu.clearCraftingContent()
        this.inventoryMenu.carried = ItemStack.EMPTY
    }

    @JvmStatic
    fun ServerPlayer.isGameMode(mode: GameType): Boolean {
        return this.gameMode.gameModeForPlayer == mode
    }

    @JvmStatic
    fun ServerPlayer.grantAdvancement(advancement: Advancement) {
        val progress = this.advancements.getOrStartProgress(advancement)
        if (!progress.isDone) {
            for (string in progress.remainingCriteria) {
                this.advancements.award(advancement, string)
            }
        }
    }

    @JvmStatic
    fun ServerPlayer.revokeAdvancement(advancement: Advancement) {
        val progress = this.advancements.getOrStartProgress(advancement)
        if (!progress.isDone) {
            for (string in progress.completedCriteria) {
                this.advancements.revoke(advancement, string)
            }
        }
    }

    @JvmStatic
    fun ServerPlayer.teleportTo(location: Location) {
        this.teleportTo(location.level, location.x, location.y, location.z, location.yaw, location.pitch)
    }

    @JvmStatic
    fun ServerPlayer.sendTitle(title: Component) {
        this.connection.send(ClientboundSetTitleTextPacket(title))
    }

    @JvmStatic
    fun ServerPlayer.sendSubtitle(subtitle: Component) {
        this.connection.send(ClientboundSetSubtitleTextPacket(subtitle))
    }

    @JvmStatic
    fun ServerPlayer.sendSound(
        sound: SoundEvent,
        source: SoundSource = SoundSource.MASTER,
        volume: Float = 1.0F,
        pitch: Float = 1.0F
    ) {
        this.playNotifySound(sound, source, volume, pitch)
    }

    @JvmStatic
    fun ServerPlayer.message(message: Component) {
        this.message(PlayerChatMessage.unsigned(this.uuid, message.string).withUnsignedContent(message))
    }

    @JvmStatic
    fun ServerPlayer.message(message: PlayerChatMessage) {
        this.server.playerList.broadcastChatMessage(message, this, ChatType.bind(ChatType.CHAT, this))
    }

    @JvmStatic
    fun ServerPlayer.teamMessage(message: Component): Boolean {
        return this.teamMessage(PlayerChatMessage.unsigned(this.uuid, message.string).withUnsignedContent(message))
    }

    @JvmStatic
    fun ServerPlayer.teamMessage(message: PlayerChatMessage): Boolean {
        val team = this.team ?: return false

        val teamDisplay = team.asPlayerTeam().displayName
        val inbound = ChatType.bind(ChatType.TEAM_MSG_COMMAND_INCOMING, this).withTargetName(teamDisplay)
        val outbound = ChatType.bind(ChatType.TEAM_MSG_COMMAND_OUTGOING, this).withTargetName(teamDisplay)

        val outgoing = OutgoingChatMessage.create(message)

        for (teammates in team.getServerPlayers()) {
            val bound = if (teammates === this) outbound else inbound
            val filter = this.shouldFilterMessageTo(teammates)
            teammates.sendChatMessage(outgoing, filter, bound)
        }
        return true
    }

    @JvmStatic
    fun ServerPlayer.distanceToWorldBorder(): Vec3 {
        val border = this.level.worldBorder
        val distanceToEast = this.x - border.minX
        val distanceToWest = border.maxX - this.x
        val distanceToNorth = this.z - border.minZ
        val distanceToSouth = border.maxZ - this.z
        val distanceToX = distanceToNorth.coerceAtMost(distanceToSouth)
        val distanceToZ = distanceToEast.coerceAtMost(distanceToWest)
        return Vec3(distanceToX, 0.0, distanceToZ)
    }

    @JvmStatic
    fun <T: ParticleOptions> ServerPlayer.sendParticles(
        options: T,
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
    fun ServerPlayer.addExtension(extension: Extension) {
        (this as ExtensionHolder).addExtension(extension)
    }

    @JvmStatic
    fun <T: Extension> ServerPlayer.getExtension(type: Class<T>): T {
        return (this as ExtensionHolder).getExtension(type)
    }

    @JvmStatic
    fun ServerPlayer.getExtensions(): Collection<Extension> {
        return (this as ExtensionHolder).getExtensions()
    }
}