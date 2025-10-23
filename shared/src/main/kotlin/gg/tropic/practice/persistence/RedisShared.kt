package gg.tropic.practice.persistence

import gg.scala.aware.thread.AwareThreadContext
import gg.tropic.practice.serializable.Message
import io.lettuce.core.api.StatefulRedisConnection
import mc.arch.commons.communications.rpc.CommunicationGateway
import net.evilblock.cubed.serializers.Serializers
import java.util.UUID

/**
 * @author GrowlyX
 * @since 9/24/2023
 */
object RedisShared
{
    val keyValueCache: StatefulRedisConnection<String, String> = CommunicationGateway("dpskv")
        .configure { internal().connect() }

    private val lobbyBridge = CommunicationGateway("lobbies")
        .apply(CommunicationGateway::start)

    val applicationBridge = CommunicationGateway("application")
        .apply(CommunicationGateway::start)

    fun redirect(players: List<UUID>, server: String)
    {
        lobbyBridge.createMessage(
            packet = "redirect",
            "playerIDs" to players,
            "server" to server
        ).publish(
            AwareThreadContext.SYNC
        )
    }

    fun sendMessage(players: List<UUID>, messages: List<String>)
    {
        lobbyBridge.createMessage(
            packet = "send-message",
            "playerIDs" to players,
            "message" to messages.joinToString("\n")
        ).publish(
            AwareThreadContext.SYNC
        )
    }

    fun sendMessage(players: List<UUID>, message: Message)
    {
        lobbyBridge.createMessage(
            packet = "send-action-message",
            "playerIDs" to players,
            "message" to Serializers.gson.toJson(message)
        ).publish(
            AwareThreadContext.SYNC
        )
    }

    fun sendBroadcast(message: Message)
    {
        lobbyBridge.createMessage(
            packet = "send-action-broadcast",
            "message" to Serializers.gson.toJson(message)
        ).publish(
            AwareThreadContext.SYNC
        )
    }

    fun sendNotificationSound(players: List<UUID>, setting: String)
    {
        lobbyBridge.createMessage(
            packet = "send-notification-sound",
            "playerIDs" to players,
            "setting" to setting
        ).publish(
            AwareThreadContext.SYNC
        )
    }
}
