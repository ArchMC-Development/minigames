package mc.arch.minigames.parties.service

import mc.arch.minigames.parties.model.Party
import java.util.UUID

/**
 * @author Subham
 * @since 7/2/25
 */
interface PartyService
{
    fun createParty(leader: UUID): Party
    fun updateParty(party: Party)
    fun warpPartyHere(party: Party)
    fun delete(party: Party)

    fun findPartyByID(id: UUID): Party?
    fun findParty(member: UUID): Party?
    fun loadedParties(): List<Party>
}
