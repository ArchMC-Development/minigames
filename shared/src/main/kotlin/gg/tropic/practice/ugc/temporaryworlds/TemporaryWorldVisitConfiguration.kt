package gg.tropic.practice.ugc.temporaryworlds

import gg.tropic.practice.ugc.generation.visits.VisitWorldRequestConfiguration

/**
 * @author Subham
 * @since 7/20/25
 */
class TemporaryWorldVisitConfiguration(
    val blockType: String
) : VisitWorldRequestConfiguration
{
    override fun getAbstractType() = TemporaryWorldVisitConfiguration::class.java
}
