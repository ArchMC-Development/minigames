package gg.tropic.practice.minigame

import java.time.Duration

/**
 * @author Subham
 * @since 5/26/25
 */
interface MiniGameEvent
{
    val description: String
    val duration: Duration

    fun execute()
    {

    }
}
