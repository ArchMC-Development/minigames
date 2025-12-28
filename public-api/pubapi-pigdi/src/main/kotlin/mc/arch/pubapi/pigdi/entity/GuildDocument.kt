package mc.arch.pubapi.pigdi.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * MongoDB document for Guild data.
 *
 * Maps to the "Guild" collection in Scala database.
 * Note: outgoingInvitations is excluded from API responses.
 *
 * @author Subham
 * @since 12/28/24
 */
@Document(collection = "Guild")
data class GuildDocument(
    @Id
    val id: String, // UUID as string (from _id)

    val identifier: String, // Same as _id

    val name: String,

    val description: String? = null,

    val createdOn: String? = null, // Date string like "Feb 4, 2025, 12:51:46 AM"

    val creator: GuildMemberData? = null,

    val members: Map<String, GuildMemberData>? = null,

    val allInvite: Boolean = false
    // Note: outgoingInvitations is intentionally excluded
)

/**
 * Guild member data.
 */
data class GuildMemberData(
    val uniqueId: String, // Player UUID

    val joinedOn: String? = null, // Date string

    val role: String? = null // "Leader", "Admin", "Member", etc.
)
