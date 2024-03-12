package net.casual.arcade.utils

import com.mojang.authlib.GameProfile
import me.lucko.fabric.api.permissions.v0.Permissions
import net.casual.arcade.Arcade
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.mixin.advancements.PlayerAdvancementsAccessor
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ExtensionUtils.addExtension
import net.casual.arcade.utils.ExtensionUtils.getExtension
import net.casual.arcade.utils.ExtensionUtils.getExtensions
import net.casual.arcade.utils.TeamUtils.asPlayerTeam
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.utils.impl.Sound
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.core.Direction8
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.network.chat.*
import net.minecraft.network.protocol.game.*
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.Action.ADD
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Predicate

public object PlayerUtils {
    private val HEALTH_BOOST: UUID = UUID.fromString("a61b8a4f-a4f5-4b7f-b787-d10ba4ad3d57")

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
    @JvmOverloads
    public fun broadcast(
        message: Component,
        filter: Predicate<ServerPlayer> = Predicate { true }
    ) {
        this.players().broadcast(message, filter)
    }

    @JvmStatic
    public fun broadcastToOps(message: Component) {
        this.players().broadcastToOps(message)
    }

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
        Arcade.getServer().sendSystemMessage(message)
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
    public fun ServerPlayer.hasPermission(permission: String, level: Int = 4): Boolean {
        return Permissions.check(this, permission, level)
    }

    @JvmStatic
    public fun SharedSuggestionProvider.hasPermission(permission: String, level: Int = 4): Boolean {
        return Permissions.check(this, permission, level)
    }

    @JvmStatic
    public fun GameProfile.hasPermission(permission: String): CompletableFuture<Boolean> {
        return Permissions.check(this, permission)
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
    public fun spread(
        level: ServerLevel,
        center: Vec2,
        distance: Double,
        range: Double,
        teams: Boolean,
        targets: Collection<ServerPlayer>,
        height: Int = level.maxBuildHeight
    ) {
        SpreadPlayers.run(level, center, distance, range, height, teams, targets)
    }

    @JvmStatic
    public fun player(name: String): ServerPlayer? {
        return Arcade.getServer().playerList.getPlayerByName(name)
    }

    @JvmStatic
    public fun player(uuid: UUID): ServerPlayer? {
        return Arcade.getServer().playerList.getPlayer(uuid)
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
                "Health Boost",
                multiply,
                AttributeModifier.Operation.MULTIPLY_BASE
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
        this.foodData.setExhaustion(0.0F)
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
    }

    @JvmStatic
    public fun ServerPlayer.revokeAllAdvancements() {
        for (advancement in this.server.advancements.allAdvancements) {
            this.revokeAdvancement(advancement)
        }
    }

    @JvmStatic
    public fun ServerPlayer.grantAllRecipes() {
        this.awardRecipes(this.server.recipeManager.recipes)
    }

    @JvmStatic
    public fun ServerPlayer.revokeAllRecipes() {
        this.resetRecipes(this.server.recipeManager.recipes)
    }

    @JvmStatic
    public fun ServerPlayer.teleportTo(location: Location) {
        this.teleportTo(location.level, location.x, location.y, location.z, location.yaw, location.pitch)
    }

    @JvmStatic
    public fun ServerPlayer.setTitleAnimation(
        fadeIn: MinecraftTimeDuration = 10.Ticks,
        stay: MinecraftTimeDuration = 70.Ticks,
        fadeOut: MinecraftTimeDuration = 20.Ticks
    ) {
        this.setTitleAnimation(fadeIn.toTicks(), stay.toTicks(), fadeOut.toTicks())
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
    @JvmOverloads
    public fun ServerPlayer.broadcastMessageAsSystem(
        message: Component,
        filter: Predicate<ServerPlayer> = Predicate { true },
        prefix: Component = this.getChatPrefix()
    ) {
        val decorated = Component.empty().append(prefix).append(message)
        broadcast(decorated, filter)
        this.server.sendSystemMessage(decorated)
    }

    @JvmStatic
    public fun ServerPlayer.getChatPrefix(withTeam: Boolean = true): MutableComponent {
        val team = this.team
        if (!withTeam || team == null) {
            return "<".literal().append(this.name).append("> ")
        }
        val name = Component.empty().append(team.playerPrefix).append(this.name).append(team.playerSuffix)
        return "<".literal().append(name).append("> ")
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