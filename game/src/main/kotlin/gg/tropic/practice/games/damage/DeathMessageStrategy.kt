package gg.tropic.practice.games.damage

import gg.tropic.game.extensions.cosmetics.CosmeticRegistry
import gg.tropic.game.extensions.cosmetics.messagebundles.KillMessageBundleCosmeticCategory
import gg.tropic.game.extensions.cosmetics.messagebundles.cosmetics.MessageBundle
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 7/20/25
 */
object DeathMessageStrategy
{
    private fun getMessageBundlePhrase(player: Player): String
    {
        val bundle = CosmeticRegistry
            .findRelatedTo(KillMessageBundleCosmeticCategory)
            .filter { like ->
                like.equipped(player)
            }
            .filterIsInstance<MessageBundle>()
            .flatMap { bundle -> bundle.phrases }

        if (bundle.isEmpty())
        {
            return "slain"
        }

        return "${CC.GOLD}${bundle.random()}${CC.GRAY}"
    }

    fun generate(
        killedBy: Player?,
        killedDisplayName: String,
        killedByDisplayName: String?,
        eliminationCause: EliminationCause,
        alternativeDeath: Boolean = false,
    ) = if (alternativeDeath && killedBy == null && eliminationCause == EliminationCause.VOID_DAMAGE)
    {
        // Handle special void case for alternative death
        "${CC.RED}$killedDisplayName${CC.GRAY} fell into the void!"
    } else
    {
        // Use the smart elimination cause formatting
        when (eliminationCause)
        {
            EliminationCause.KILLED_BY_PLAYER ->
            {
                // Only show "slain by X" if we have a valid killer name
                if (killedByDisplayName != null) {
                    val phrase = if (killedBy is Player)
                        getMessageBundlePhrase(killedBy) else "slain"

                    "${CC.GREEN}$killedDisplayName${CC.GRAY} was $phrase by ${CC.RED}$killedByDisplayName${CC.GRAY}!"
                } else {
                    // No killer context - show generic death
                    "${CC.RED}$killedDisplayName${CC.GRAY} died!"
                }
            }

            else -> eliminationCause.formatMessage(
                killedDisplayName,
                killedByDisplayName  // This is already nullable and handled by formatMessage
            )
        }
    }
}
