package mc.arch.pubapi.pigdi.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * MongoDB document for Clan data (Trojan/Lifesteal gamemode).
 *
 * @author Subham
 * @since 12/28/24
 */
@Document(collection = "Clan")
data class ClanDocument(
    @Id
    val id: String, // UUID as string

    val identifier: String = id,

    val name: String,

    val displayName: String,

    val stringId: String,

    val description: String? = null,

    val leader: String, // UUID as string

    val members: List<String> = emptyList(), // List of member UUIDs

    val invitations: List<String> = emptyList(),

    val experience: String = "0", // Stored as string in Scala DB

    val calculationsPaused: Boolean = false
)
{
    /**
     * Calculate the clan level from experience.
     * Level = experience / 1000, rounded to 3 decimal places.
     */
    fun calculateLevel(): Double
    {
        val exp = experience.toLongOrNull() ?: 0L
        return (exp / 1000.0 * 1000).toLong() / 1000.0 // Round to 3 decimals
    }
}
