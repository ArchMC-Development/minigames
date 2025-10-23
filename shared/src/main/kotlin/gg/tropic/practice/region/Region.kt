package gg.tropic.practice.region

import lol.arch.symphony.api.model.TrackedPlayer

/**
 * @author GrowlyX
 * @since 12/17/2023
 */
enum class Region
{
    NA, EU, Both;

    fun withinScopeOf(region: Region) = this == Both || region == this || region == Both

    companion object
    {
        @JvmStatic
        fun extractFrom(id: String) = when (true)
        {
            id.startsWith("na") -> NA
            id.startsWith("eu") -> EU
            else -> NA
        }

        @JvmStatic
        fun extractFromTrackedPlayer(trackedPlayer: TrackedPlayer?) = extractFrom(trackedPlayer?.instance ?: "")
    }
}
