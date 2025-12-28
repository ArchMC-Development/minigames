package mc.arch.pubapi.pigdi.repository

import mc.arch.pubapi.pigdi.entity.GuildDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * MongoDB repository for Guild documents.
 *
 * @author Subham
 * @since 12/28/24
 */
@Repository
interface GuildRepository : MongoRepository<GuildDocument, String>
