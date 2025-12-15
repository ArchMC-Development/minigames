package mc.arch.minigames.persistent.housing.game.resources

import gg.tropic.practice.ugc.resources.HostedWorldInstancePlayerResources

data class HousingPlayerResources(
    override val username: String,
    override val displayName: String,
    override val disguised: Boolean
) : HostedWorldInstancePlayerResources
