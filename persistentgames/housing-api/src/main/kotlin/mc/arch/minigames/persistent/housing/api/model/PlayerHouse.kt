package mc.arch.minigames.persistent.housing.api.model

import gg.scala.commons.annotations.Model
import gg.scala.store.storage.storable.IDataStoreObject
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequestConfiguration
import mc.arch.minigames.persistent.housing.api.content.HousingItemStack
import mc.arch.minigames.persistent.housing.api.entity.HousingNPC
import mc.arch.minigames.persistent.housing.api.spatial.WorldPosition
import java.util.UUID

/**
 * @author Subham
 * @since 9/8/25
 */
@Model
data class PlayerHouse(
    val owner: UUID,
    val spawnPoint: WorldPosition? = null,
    val houseIcon: HousingItemStack? = null,
    val houseNPCMap: MutableMap<String, HousingNPC> = mutableMapOf(),
    override val identifier: UUID = UUID.randomUUID(),
) : IDataStoreObject
