package mc.arch.minigames.persistent.housing.api

import gg.tropic.practice.ugc.PersistencePolicy
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequestConfiguration
import java.util.UUID

/**
 * @author Subham
 * @since 9/8/25
 */
class VisitHouseConfiguration(
    val houseId: UUID,
    val persistencePolicy: PersistencePolicy
) : VisitWorldRequestConfiguration
{
    override fun getAbstractType() = VisitHouseConfiguration::class.java
}
