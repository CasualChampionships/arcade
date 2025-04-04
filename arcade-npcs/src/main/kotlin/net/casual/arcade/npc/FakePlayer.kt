package net.casual.arcade.npc

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.PropertyMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.npc.ai.NPCJumpControl
import net.casual.arcade.npc.ai.NPCLookControl
import net.casual.arcade.npc.ai.NPCMoveControl
import net.casual.arcade.npc.network.FakeConnection
import net.casual.arcade.npc.network.FakeGamePacketListenerImpl
import net.casual.arcade.npc.network.FakeLoginPacketListenerImpl
import net.casual.arcade.npc.pathfinding.NPCGroundPathNavigation
import net.casual.arcade.npc.pathfinding.NPCPathNavigation
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.Util
import net.minecraft.core.UUIDUtil
import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.level.pathfinder.PathType
import java.util.*
import java.util.concurrent.CompletableFuture

public open class FakePlayer protected constructor(
    server: MinecraftServer,
    level: ServerLevel,
    profile: GameProfile
): ServerPlayer(server, level, profile, ClientInformation.createDefault()) {
    public val moveControl: NPCMoveControl = NPCMoveControl(this)
    public val lookControl: NPCLookControl = NPCLookControl(this)
    public val jumpControl: NPCJumpControl = NPCJumpControl(this)
    public val navigation: NPCPathNavigation = NPCGroundPathNavigation(this, level)

    public var follow: ServerPlayer? = null

    // FIXME: This needs to be implemented properly
    public fun getPathfindingMalus(type: PathType): Float {
        return type.malus
    }

    public open fun createRespawned(
        server: MinecraftServer,
        level: ServerLevel,
        profile: GameProfile
    ): FakePlayer {
        return FakePlayer(server, level, profile)
    }

    public open fun createConnection(
        server: MinecraftServer,
        connection: Connection,
        cookie: CommonListenerCookie
    ): FakeGamePacketListenerImpl {
        return FakeGamePacketListenerImpl(server, connection, this, cookie)
    }

    public open fun connection(): FakeGamePacketListenerImpl {
        return this.connection as FakeGamePacketListenerImpl
    }

    override fun tick() {
        // The player will never send move packets,
        // so we need to manually move the player.
        // This keeps the ticket manager updated
        if (this.server.tickCount % 10 == 0) {
            this.connection.resetPosition()
            this.serverLevel().chunkSource.move(this)
        }
        super.tick()
    }

    override fun serverAiStep() {
        super.serverAiStep()

        if (this.follow != null) {
            this.navigation.moveTo(this.follow!!, 1.0)
        }

        this.navigation.tick()

        this.moveControl.tick()
        this.lookControl.tick()
        this.jumpControl.tick()
    }

    override fun tickDeath() {
        this.connection.handleClientCommand(
            ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN)
        )
    }

    override fun forceSetRotation(yRot: Float, xRot: Float) {
        this.yRot = yRot
        this.setYHeadRot(yRot)
        this.xRot = xRot
        this.setOldRot()
    }

    override fun isControlledByClient(): Boolean {
        return false
    }

    override fun showEndCredits() {
        this.wonGame = true
        super.showEndCredits()
    }

    public companion object {
        private val joining = Object2ObjectOpenHashMap<String, CompletableFuture<FakePlayer>>()

        public fun join(server: MinecraftServer, profile: GameProfile): CompletableFuture<FakePlayer> {
            val connection = FakeConnection()
            // We simulate the fake login packet listener for luckperms compatability
            val login = FakeLoginPacketListenerImpl(server, connection, profile)
            return login.handleQueries().thenApplyAsync({
                if (server.playerList.getPlayer(profile.id) != null) {
                    throw IllegalArgumentException("Player with UUID ${profile.id} already exists")
                }

                val player = FakePlayer(server, server.overworld(), profile)
                player.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, 0x7F)
                server.playerList.placeNewPlayer(
                    connection, player, CommonListenerCookie(profile, 0, player.clientInformation(), false)
                )
                server.connection.connections.add(connection)
                player.connection.handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket())
                player
            }, server)
        }

        public fun join(server: MinecraftServer, username: String): CompletableFuture<FakePlayer> {
            return this.joining.getOrPut(username) {
                val resolvable = ResolvableProfile(Optional.of(username), Optional.empty(), PropertyMap())
                resolvable.resolve().whenCompleteAsync({ _, throwable ->
                    this.joining.remove(username)
                    if (throwable != null) {
                        ArcadeUtils.logger.error("Fake player $username failed to join", throwable)
                    }
                }, server).thenCompose { resolved ->
                    val profile = if (resolved.id.get() == Util.NIL_UUID) {
                        GameProfile(UUIDUtil.createOfflinePlayerUUID(username), username)
                    } else {
                        resolved.gameProfile
                    }
                    this.join(server, profile)
                }
            }
        }

        public fun join(server: MinecraftServer, uuid: UUID): CompletableFuture<FakePlayer> {
            val resolvable = ResolvableProfile(Optional.empty(), Optional.of(uuid), PropertyMap())
            return resolvable.resolve().thenComposeAsync({ resolved ->
                if (resolved.name.get().isEmpty()) {
                    throw IllegalStateException("Resolved name was empty")
                }
                this.join(server, resolved.gameProfile)
            }, server)
        }

        public fun isJoining(username: String): Boolean {
            return this.joining.containsKey(username)
        }
    }
}