package gg.tropic.practice.minigame

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.tropic.practice.configuration.PracticeConfigurationService
import gg.tropic.practice.games.GameState
import gg.tropic.practice.metadata.SystemMetadataService
import java.util.concurrent.CompletableFuture

/**
 * @author Subham
 * @since 6/27/25
 */
open class MiniGameTypeMetadata(
    val internalId: String,
    val displayName: String,
    val item: XMaterial,
    val lobbyGroup: String,
    val gameModes: Map<String, MiniGameModeMetadata>,
    val autoJoinSkinValue: String,
    val autoJoinSkinSignature: String,
)
{
    fun computeGameTypeRequiringPlayers(): CompletableFuture<MiniGameModeMetadata> =
        CompletableFuture.supplyAsync {
            SystemMetadataService.allGames()
                .filter { it.miniGameType == internalId }
                .filter { it.state == GameState.Waiting || it.state == GameState.Starting }
                .map {
                    val mode = PracticeConfigurationService
                        .minigameType().provide()
                        .gameModes.values
                        .firstOrNull { metadata -> metadata.kitID == it.kitID }
                        ?: return@map null to 0.0

                    mode to (it.players.size.toDouble() / mode.mode
                        .maxPlayers().toDouble())
                }
                .filter { it.second < 1.0 }
                .maxByOrNull { it.second }
                ?.first
                ?: gameModes.values.first()
        }

    fun mode(id: String) = gameModes[id]!!
    fun modeNullable(id: String) = gameModes[id]

    fun totalPlayersPlaying() = gameModes.values.sumOf { it.playersPlaying() }
    fun totalPlayersInLobby() = ServerContainer
        .getServersInGroupCasted<GameServer>(lobbyGroup)
        .sumOf { it.getPlayersCount() ?: 0 }
}
