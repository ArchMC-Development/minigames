package gg.tropic.practice.commands.admin.matchlist

import gg.tropic.practice.games.GameReference

/**
 * @author GrowlyX
 * @since 3/24/2025
 */
enum class MatchListSort(val sort: List<GameReference>.() -> List<GameReference>)
{
    Kit({
        sortedBy {
            it.kitID
        }
    }),
    Map({
        sortedBy {
            it.mapID
        }
    }),
    InstanceID({
        sortedBy {
            it.server
        }
    }),
    ParticipatingPlayerCount({
        sortedBy {
            it.players.size
        }
    }),
    SpectatorCount({
        sortedByDescending {
            it.spectators.size
        }
    }),
    GameState({
        sortedBy {
            it.state
        }
    });

    fun previous(): MatchListSort
    {
        return MatchListSort.entries
            .getOrNull(ordinal - 1)
            ?: MatchListSort.entries.last()
    }

    fun next(): MatchListSort
    {
        return MatchListSort.entries
            .getOrNull(ordinal + 1)
            ?: MatchListSort.entries.first()
    }
}
