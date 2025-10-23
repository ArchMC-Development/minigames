package gg.tropic.practice.games.damage

import net.evilblock.cubed.util.CC

/**
 * @author Subham
 * @since 7/20/25
 */
enum class EliminationCause(
    val displayName: String,
    val canHavePlayerContext: Boolean = false,
    val priority: Int = 0
)
{
    // Environmental causes
    FALL_DAMAGE("fell to their death", true, 1),
    VOID_DAMAGE("fell into the void", true, 3),
    LAVA_DAMAGE("burned to death", true, 2),
    DROWNING("drowned", true, 1),
    SUFFOCATION("suffocated", true, 1),
    FIRE_DAMAGE("burned to death", true, 2),
    EXPLOSION("was blown up", true, 4),

    // Entity-based causes
    KILLED_BY_PLAYER("was slain", true, 5),
    PROJECTILE_BY_PLAYER("was shot", true, 6),
    PROJECTILE_BY_MOB("was shot by a mob", false, 4),

    // Special causes
    LIGHTNING("was struck by lightning", false, 3),
    POISON("died from poison", true, 2),
    WITHER("withered away", true, 2),
    MAGIC("died from magic", true, 3),
    THORNS("was pricked to death", true, 1),

    // Unknown/Generic
    UNKNOWN("died", false, 0);

    fun formatMessage(victimName: String, contextPlayer: String? = null): String
    {
        return when
        {
            contextPlayer != null && canHavePlayerContext ->
            {
                when (this)
                {
                    FALL_DAMAGE -> "${CC.RED}$victimName${CC.GRAY} was knocked off a cliff by ${CC.GREEN}$contextPlayer${CC.GRAY}!"
                    VOID_DAMAGE -> "${CC.RED}$victimName${CC.GRAY} was knocked into the void by ${CC.GREEN}$contextPlayer${CC.GRAY}!"
                    LAVA_DAMAGE -> "${CC.RED}$victimName${CC.GRAY} was knocked into lava by ${CC.GREEN}$contextPlayer${CC.GRAY}!"
                    DROWNING -> "${CC.RED}$victimName${CC.GRAY} was pushed underwater by ${CC.GREEN}$contextPlayer${CC.GRAY}!"
                    EXPLOSION -> "${CC.RED}$victimName${CC.GRAY} was blown up by ${CC.GREEN}$contextPlayer${CC.GRAY}!"
                    PROJECTILE_BY_PLAYER -> "${CC.RED}$victimName${CC.GRAY} was shot by ${CC.GREEN}$contextPlayer${CC.GRAY}!"
                    KILLED_BY_PLAYER -> "${CC.RED}$victimName${CC.GRAY} was slain by ${CC.GREEN}$contextPlayer${CC.GRAY}!"
                    else -> "${CC.RED}$victimName${CC.GRAY} $displayName by ${CC.GREEN}$contextPlayer${CC.GRAY}!"
                }
            }

            else -> "${CC.RED}$victimName${CC.GRAY} $displayName!"
        }
    }
}
