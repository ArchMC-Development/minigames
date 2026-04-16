package mc.arch.minigames.persistent.housing.api.config

/**
 * Global housing configuration synced across all housing servers.
 *
 * @author ArchMC
 */
data class HousingConfiguration(
    var featureHouseCostPerWeek: Long = 1000L,
    var allowHouseCreation: Boolean = true,
    var allowVisiting: Boolean = true
)
