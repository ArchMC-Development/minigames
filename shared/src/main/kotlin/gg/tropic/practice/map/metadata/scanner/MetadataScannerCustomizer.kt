package gg.tropic.practice.map.metadata.scanner

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommandManager

/**
 * @author Subham
 * @since 5/23/25
 */
object MetadataScannerCustomizer
{
    @CommandManagerCustomizer
    fun customize(manager: ScalaCommandManager)
    {
        manager.commandContexts.registerContext(AbstractMapMetadataScanner::class.java) {
            val firstArgument = it.popFirstArg()
            MetadataScannerUtilities.matches(firstArgument)
                ?: throw ConditionFailedException(
                    "$firstArgument is not a metadata scanner."
                )
        }

        manager.commandCompletions
            .registerAsyncCompletion("metadata-scanners") {
                MetadataScannerUtilities.scanners.map { it.type }
            }
    }
}
