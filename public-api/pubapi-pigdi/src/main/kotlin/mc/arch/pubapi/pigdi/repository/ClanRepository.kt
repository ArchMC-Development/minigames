package mc.arch.pubapi.pigdi.repository

import mc.arch.pubapi.pigdi.entity.ClanDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * MongoDB repository for Clan documents.
 *
 * @author Subham
 * @since 12/28/24
 */
@Repository
interface ClanRepository : MongoRepository<ClanDocument, String>
