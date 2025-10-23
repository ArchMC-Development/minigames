package gg.tropic.practice.games.pvpclasses

import org.apache.commons.lang.StringUtils
import org.bukkit.Material

enum class PvPClasses(
    val icon: Material,
    val maxForFive: Int,
    val maxForTen: Int,
    val maxForTwenty: Int
) {
    DIAMOND(Material.DIAMOND_CHESTPLATE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE),
    BARD(Material.GOLD_CHESTPLATE, 2, 2, 4),
    ARCHER(Material.LEATHER_CHESTPLATE, 2, 2, 4),
    ROGUE(Material.CHAINMAIL_CHESTPLATE, 2, 2, 4);

    fun isAllowed(party: Party): Boolean {
        if (this == DIAMOND) return true

        val current = party.kits.values.count { it == this }
        val size = party.members.size

        val tournament = PotPvPSI.getInstance().tournamentHandler.tournament

        return if (tournament != null && tournament.isInTournament(party)) {
            handleTournamentAllowance(tournament, current)
        } else {
            handleRegularAllowance(size, current)
        }
    }

    fun isAllowed(team: RankedGameTeam): Boolean {
        if (this == DIAMOND) return true

        val current = team.kits.values.count { it == this }
        return current < 1
    }

    fun getName(): String = StringUtils.capitalize(name.lowercase())

    private fun handleTournamentAllowance(tournament: com.elevatemc.potpvp.tournament.Tournament, current: Int): Boolean {
        val gameType = tournament.type
        if (gameType != GameModes.TEAMFIGHT && gameType != GameModes.TEAMFIGHT_DEBUFF) {
            return false
        }

        return when (this) {
            ROGUE -> false
            BARD -> current < tournament.bards
            ARCHER -> current < tournament.archers
            DIAMOND -> true
        }
    }

    private fun handleRegularAllowance(size: Int, current: Int): Boolean {
        return when {
            size < 10 && current >= maxForFive -> false
            size < 20 && current >= maxForTen -> false
            else -> current < maxForTwenty
        }
    }
}
