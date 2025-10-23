package gg.solara.practice.customizer

import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommandManager
import gg.tropic.game.extensions.cosmetics.cages.SchemaCageCommandCustomizers

/**
 * @author GrowlyX
 * @since 8/25/2024
 */
object DevToolsCommandCustomizer
{
    @CommandManagerCustomizer
    fun customize(manager: ScalaCommandManager) = SchemaCageCommandCustomizers.customize(manager)
}
