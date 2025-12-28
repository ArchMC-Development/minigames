package mc.arch.pubapi.pigdi.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

/**
 * MongoDB document for LifestealProfile (Trojan gamemode).
 *
 * Maps to the "LifestealProfile" collection.
 *
 * @author Subham
 * @since 12/28/24
 */
@Document(collection = "LifestealProfile")
data class LifestealProfileDocument(
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

/**
 * Home entry data (only public flag exposed, locations stripped).
 */
data class HomeEntry(
    val location: Any? = null, // Not mapped to response
    val server: String? = null, // Not mapped to response
    @Field("public")
    val public: Boolean = false
)

/**
 * Item filter configuration.
 */
data class ItemFilterData(
    val enabled: Boolean = false,
    val type: String = "Blacklist",
    val excludedBlocks: List<String>? = null,
    val excludedPotions: List<String>? = null
)
{
    fun getExcludedBlockCount(): Int = excludedBlocks?.size ?: 0
    fun getExcludedPotionCount(): Int = excludedPotions?.size ?: 0
}
