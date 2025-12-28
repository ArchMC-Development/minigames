package mc.arch.pubapi.pigdi.repository

import mc.arch.pubapi.pigdi.entity.SurvivalProfileDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * MongoDB repository for SurvivalProfile documents.
 *
 * @author Subham
 * @since 12/28/24
 */
@Repository
interface SurvivalProfileRepository : MongoRepository<SurvivalProfileDocument, String>
{
    fun findByIdentifier(identifier: String): SurvivalProfileDocument?
}
