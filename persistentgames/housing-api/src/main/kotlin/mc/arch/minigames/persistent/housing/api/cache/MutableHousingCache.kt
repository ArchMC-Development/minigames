package mc.arch.minigames.persistent.housing.api.cache

import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import java.util.UUID

/**
 * Class created on 12/18/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
//todo: maybe better way to store this? not sure tbh im tired
object MutableHousingCache
{
    private val housingMap = mutableMapOf<UUID, PlayerHouse>()

    fun cached(uniqueId: UUID) = housingMap[uniqueId]

    fun cache(house: PlayerHouse)
    {
        housingMap[house.identifier] = house
    }

    fun uncache(uniqueId: UUID) = housingMap.remove(uniqueId)
}