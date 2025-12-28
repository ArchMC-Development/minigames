package mc.arch.pubapi.pigdi.repository

import mc.arch.pubapi.pigdi.entity.LifestealProfileDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * MongoDB repository for LifestealProfile documents.
 *
 * @author Subham
 * @since 12/28/24
 */
@Repository
interface LifestealProfileRepository : MongoRepository<LifestealProfileDocument, String>
{
    fun findByIdentifier(identifier: String): LifestealProfileDocument?
}
