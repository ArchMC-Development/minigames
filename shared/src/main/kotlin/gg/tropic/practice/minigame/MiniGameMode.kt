package gg.tropic.practice.minigame

import gg.tropic.practice.provider.MiniProviderVersion

/**
 * @author Subham
 * @since 6/15/25
 */
interface MiniGameMode
{
    val teamSize: Int
    val teamCount: Int
    val providerVersion: MiniProviderVersion get() = MiniProviderVersion.LEGACY

    fun maxPlayers(): Int
}
