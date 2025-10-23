package gg.tropic.practice.commands.admin.matchlist

import gg.tropic.practice.games.GameReference

/**
 * @author GrowlyX
 * @since 3/24/2025
 */
enum class MatchListFilter(val filter: List<GameReference>.() -> List<GameReference>)
{
    All({
        this
    }),
    SkyWars({
        filter {
            it.miniGameType == "skywars"
        }
    }),
    BedWars({
        filter {
            it.miniGameType == "bedwars"
        }
    }),
    Duels({
        filter {
            it.miniGameType == null
        }
    });

    fun previous(): MatchListFilter
    {
        return entries
            .getOrNull(ordinal - 1)
            ?: entries.last()
    }

    fun next(): MatchListFilter
    {
        return entries
            .getOrNull(ordinal + 1)
            ?: entries.first()
    }
}
