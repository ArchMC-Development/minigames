package mc.arch.pubapi.akers.service

import gg.scala.commons.persist.ProfileOrchestrator
import gg.scala.flavor.service.Service
import mc.arch.pubapi.akers.model.AkersProfile
import java.util.*

/**
 * @author Subham
 * @since 12/27/24
 */
@Service
object AkersProfileService : ProfileOrchestrator<AkersProfile>()
{
    override fun new(uniqueId: UUID) = AkersProfile(uniqueId)
    override fun type() = AkersProfile::class

    init
    {
        // We eagerly save the profile when needed, so this is no longer needed.
        enableReadOnlyProfile()
    }
}
