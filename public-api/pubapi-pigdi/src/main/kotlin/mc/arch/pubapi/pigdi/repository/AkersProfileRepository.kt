package mc.arch.pubapi.pigdi.repository

import mc.arch.pubapi.pigdi.entity.AkersProfileDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * MongoDB repository for AKERS profiles.
 *
 * @author Subham
 * @since 12/27/24
 */
@Repository
interface AkersProfileRepository : MongoRepository<AkersProfileDocument, String>
{
    fun findByDiscordId(discordId: String): AkersProfileDocument?
}
