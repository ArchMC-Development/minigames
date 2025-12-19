package mc.arch.minigames.persistent.housing.api.model

import gg.scala.common.Savable
import gg.scala.commons.annotations.Model
import gg.scala.store.storage.storable.IDataStoreObject
import gg.tropic.practice.ugc.HostedWorldAttribute
import mc.arch.minigames.persistent.housing.api.action.tasks.Task
import mc.arch.minigames.persistent.housing.api.content.HousingGameMode
import mc.arch.minigames.persistent.housing.api.content.HousingItemStack
import mc.arch.minigames.persistent.housing.api.entity.HousingHologram
import mc.arch.minigames.persistent.housing.api.entity.HousingNPC
import mc.arch.minigames.persistent.housing.api.role.HouseRole
import mc.arch.minigames.persistent.housing.api.service.PlayerHousingService
import mc.arch.minigames.persistent.housing.api.spatial.WorldPosition
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * @author Subham
 * @since 9/8/25
 */
@Model
data class PlayerHouse(
    val owner: UUID,
    override val name: String,
    var spawnPoint: WorldPosition? = null,
    var defaultGamemode: HousingGameMode = HousingGameMode.SURVIVAL,
    var maxPlayers: Int = 20,
    var plotSizeBlocks: Int = 200,
    val tags: MutableList<String> = mutableListOf(),
    val actionEventMap: MutableMap<String, Task<*>> = mutableMapOf(),
    val roles: MutableMap<String, HouseRole> = HouseRole.defaults(),
    val visitationStatuses: MutableMap<VisitationStatus, Boolean> = mutableMapOf(
        VisitationStatus.PRIVATE to true,
    ),
    val playerRoles: MutableMap<UUID, String> = mutableMapOf(),
    val houseIcon: HousingItemStack? = null,
    val houseNPCMap: MutableMap<String, HousingNPC> = mutableMapOf(),
    val houseHologramMap: MutableMap<String, HousingHologram> = mutableMapOf(),
    override val identifier: UUID = UUID.randomUUID(),
    override val displayName: String = name,
    override val description: MutableList<String> = mutableListOf()
) : IDataStoreObject, HostedWorldAttribute, Savable
{

    fun visitationStatusApplies(status: VisitationStatus) = visitationStatuses[status] == true

    fun playerCanJoin(uuid: UUID): CompletableFuture<Boolean> {
        if (visitationStatuses[VisitationStatus.PUBLIC] == true)
        {
            return CompletableFuture.completedFuture(true)
        }

        return CompletableFuture.completedFuture(uuid == owner)
    }

    fun playerIsOrAboveAdministrator(player: UUID): Boolean = owner == player || hasPermission(player, "house.manage")

    fun hasPermission(player: UUID, permission: String) = getRole(player).permissions.contains(permission)

    fun getRoleByName(name: String) = roles[name.lowercase()]

    fun getRole(player: UUID): HouseRole {
        val defaultRole = roles.values.first { it.default }
        val roleString = playerRoles[player]
            ?: return defaultRole

        return getRoleByName(roleString) ?: defaultRole
    }

    override fun save(): CompletableFuture<Void> = PlayerHousingService.save(this)
}
