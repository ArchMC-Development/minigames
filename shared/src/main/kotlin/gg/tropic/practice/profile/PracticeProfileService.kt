package gg.tropic.practice.profile

import gg.scala.commons.graduation.Schoolmaster
import gg.scala.commons.persist.ProfileOrchestrator
import gg.scala.flavor.service.Service
import gg.tropic.practice.statistics.StatisticService
import io.lettuce.core.ScoredValue
import me.lucko.helper.Schedulers
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author GrowlyX
 * @since 9/17/2023
 */
@Service
object PracticeProfileService : ProfileOrchestrator<PracticeProfile>()
{
    override fun new(uniqueId: UUID) = PracticeProfile(uniqueId)
    override fun type() = PracticeProfile::class

    private val schoolMaster = Schoolmaster<PracticeProfile>().apply {
        stage("v2-statistics") {
            statistics()
        }
    }

    init
    {
        // We eagerly save the profile when needed, so this is no longer needed.
        enableReadOnlyProfile()
    }

    override fun postLoad(uniqueId: UUID)
    {
        val profile = find(uniqueId)
            ?: return

        if (schoolMaster.mature(profile))
        {
            profile.save()
        }
    }
}
