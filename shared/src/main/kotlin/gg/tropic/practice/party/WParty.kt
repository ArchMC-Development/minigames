package gg.tropic.practice.party

import mc.arch.minigames.parties.model.Party
import java.util.UUID

/**
 * @author GrowlyX
 * @since 2/9/2024
 */
data class WParty(var delegate: Party)
{
    var currentPlayers = 0
    var currentPlayersIDs = listOf<UUID>()
    fun update(party: Party)
    {
        this.delegate = party
    }

    fun toParty() = delegate
    fun onlinePlayers() = delegate.includedMembers()
}
