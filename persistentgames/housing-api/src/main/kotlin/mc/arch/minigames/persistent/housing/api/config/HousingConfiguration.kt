package mc.arch.minigames.persistent.housing.api.config

import gg.scala.commons.graduation.Progressive

data class HousingConfiguration(
    var featureHouseCostPerWeek: Long = 1000L,
    var allowHouseCreation: Boolean = true,
    var allowVisiting: Boolean = true,
    var allowRentingSlots: Boolean = false,
    override var matured: Set<String>?
): Progressive
