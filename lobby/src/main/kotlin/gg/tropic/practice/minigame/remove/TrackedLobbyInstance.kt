package gg.tropic.practice.minigame.remove

import gg.scala.commons.agnostic.sync.server.impl.GameServer

/**
 * @author GrowlyX
 * @since 1/25/2025
 */
data class TrackedLobbyInstance(
    val gameServer: GameServer,
    val friendlyId: Int
)
{
    fun isDraining() = gameServer
        .getMetadataValue<Boolean>("server", "kubernetes-draining") ?: false
}
