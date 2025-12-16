package mc.arch.minigames.persistent.housing.api.model

import gg.scala.commons.annotations.Model
import gg.scala.store.storage.storable.IDataStoreObject
import gg.tropic.practice.ugc.HostedWorldAttribute
import mc.arch.minigames.persistent.housing.api.content.HousingGameMode
import mc.arch.minigames.persistent.housing.api.content.HousingItemStack
import mc.arch.minigames.persistent.housing.api.entity.HousingNPC
import mc.arch.minigames.persistent.housing.api.role.HouseRole
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
    val roles: MutableMap<String, HouseRole> = mutableMapOf(
        "guest" to HouseRole(
            name = "guest",
            permissions = mutableListOf(),
            color = "&7",
            default = true,
        ),
        "resident" to HouseRole(
            name = "resident",
            permissions = mutableListOf("house.interact"),
            color = "&6",
        ),
        "co-owner" to HouseRole(
            name = "co-owner",
            permissions = mutableListOf("house.manager"),
            color = "&e",
        ),
        "owner" to HouseRole(
            name = "owner",
            permissions = mutableListOf("house.owner"),
            color = "&c",
        ),
    ),
    val visitationStatuses: MutableMap<VisitationStatus, Boolean> = mutableMapOf(
        VisitationStatus.PRIVATE to true,
    ),
    val playerRoles: MutableMap<UUID, String> = mutableMapOf(),
    val houseIcon: HousingItemStack? = null,
    val houseNPCMap: MutableMap<String, HousingNPC> = mutableMapOf(),
    override val identifier: UUID = UUID.randomUUID(),
    override val displayName: String = name,
    override val description: MutableList<String> = mutableListOf()
) : IDataStoreObject, HostedWorldAttribute
{
    fun playerCanJoin(uuid: UUID): CompletableFuture<Boolean> {
        if (visitationStatuses[VisitationStatus.PUBLIC] == true)
        {
            return CompletableFuture.completedFuture(true)
        }

        return CompletableFuture.completedFuture(uuid == owner)
    }

    fun getRoleByName(name: String) = roles[name.lowercase()]

    fun getRole(player: UUID): HouseRole {
        val defaultRole = roles.values.first { it.default }
        val roleString = playerRoles[player]
            ?: return defaultRole

        return getRoleByName(roleString) ?: defaultRole
    }
}
