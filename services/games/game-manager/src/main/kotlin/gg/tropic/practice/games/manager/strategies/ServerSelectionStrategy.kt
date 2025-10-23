package gg.tropic.practice.games.manager.strategies

import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.commons.agnostic.sync.server.state.ServerState
import gg.tropic.practice.gameGroup
import gg.tropic.practice.games.manager.GameManager
import gg.tropic.practice.provider.MiniProviderType
import gg.tropic.practice.provider.MiniProviderVersion
import gg.tropic.practice.region.Region
import gg.tropic.practice.suffixWhenDev
import gg.tropic.practice.ugc.WorldInstanceProviderType

/**
 * @author Subham
 * @since 7/20/25
 */
object ServerSelectionStrategy
{
    fun select(
        requiredVersion: MiniProviderVersion,
        requiredType: MiniProviderType,
        region: Region,
        excludeInstance: String? = null,
        minigameOrchestratorID: String? = null,
        selectNewestInstance: Boolean = false,
        hostedWorldInstanceProviderType: WorldInstanceProviderType? = null
    ): GameServer?
    {
        val instances = GameManager.instances()
        return ServerContainer
            .getServersInGroupCasted<GameServer>(gameGroup().suffixWhenDev())
            .filter {
                if (excludeInstance != null && it.id == excludeInstance)
                {
                    return@filter false
                }

                val serverInstance = instances[it.id]

                !it.isDraining() &&
                    it.state == ServerState.Loaded &&
                    serverInstance != null &&
                    serverInstance.version == requiredVersion &&
                    (hostedWorldInstanceProviderType == null ||
                        serverInstance.supportedHostedWorldProviderTypes.contains(hostedWorldInstanceProviderType)) &&
                    (minigameOrchestratorID == null ||
                        serverInstance.availableMinigames.contains(minigameOrchestratorID)) &&
                    serverInstance.supportedTypes.contains(requiredType)
            }
            .let { stream ->
                if (!selectNewestInstance)
                {
                    stream.sortedBy(GameServer::getPlayersCount)
                } else
                {
                    stream.sortedByDescending(GameServer::initialHeartbeat)
                }
            }
            .firstOrNull {
                // ensure server of NEW replication is in the same region
                Region.extractFrom(it.id).withinScopeOf(region)
            }
    }
}
