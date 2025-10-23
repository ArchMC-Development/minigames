package gg.tropic.practice.integration

import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan

/**
 * @author GrowlyX
 * @since 1/16/2024
 */
@Service
@IgnoreAutoScan
@SoftDependency("ScStaff")
object StaffACProvider
{
    @Configure
    fun configure()
    {
        // disable the anticheat in practice lobbies
    }
}
