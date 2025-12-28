package mc.arch.pubapi.pigdi.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * MongoDB document for SurvivalProfile (Spartan gamemode).
 *
 * Maps to the "SurvivalProfile" collection.
 * Structure is identical to LifestealProfile.
 *
 * @author Subham
 * @since 12/28/24
 */
@Document(collection = "SurvivalProfile")
data class SurvivalProfileDocument(
    @Id
    val id: String, // UUID as string (from _id)

    val identifier: String, // Player UUID

    val additionalStorageMap: Map<String, String>? = null,

    val homeList: List<HomeEntry?>? = null,

    val ignoredTips: List<String>? = null,

    val itemFilter: ItemFilterData? = null,

    val renameTokens: Int = 0,

    val totalPlaytime: String? = null, // Stored as string, needs parsing

    val hasMigratedV1Homes: Boolean? = null,

    val hasMigratedV2Homes: Boolean? = null
)
{
    /**
     * Get total playtime in seconds.
     */
    fun getPlaytimeSeconds(): Long
    {
        return totalPlaytime?.toLongOrNull() ?: 0L
    }

    /**
     * Get hearts from additionalStorageMap.
     */
    fun getHearts(): Int?
    {
        return additionalStorageMap?.get("hearts")?.toIntOrNull()
    }

    /**
     * Count non-null homes.
     */
    fun getHomeCount(): Int
    {
        return homeList?.count { it != null } ?: 0
    }

    /**
     * Count public homes.
     */
    fun getPublicHomeCount(): Int
    {
        return homeList?.count { it?.public == true } ?: 0
    }
}
