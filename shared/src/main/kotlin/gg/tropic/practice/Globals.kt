package gg.tropic.practice

import java.util.UUID

/**
 * @author GrowlyX
 * @since 7/16/2024
 */
object Globals
{
    @JvmStatic
    val POSSIBLE_PLAYER_BOT_UNIQUE_IDS = (1..99)
        .map {
            if (it > 9)
            {
                return@map UUID.fromString("00000000-0000-0000-0000-0000000000$it")
            }

            UUID.fromString("00000000-0000-0000-0000-00000000000$it")
        }
        .toTypedArray()
}
