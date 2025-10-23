package gg.tropic.practice.application.platform

import gg.scala.commons.PlatformBundle

/**
 * @author GrowlyX
 * @since 2/27/2025
 */
object PracticeAPIPlatform : PlatformBundle
{
    override fun profileOrchestratorService() = throw IllegalStateException("Cannot do this on app")
    override fun utilities() = throw IllegalStateException("Cannot do this on app")
}
