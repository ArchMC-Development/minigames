package gg.tropic.practice.minigame

import me.lucko.helper.terminable.composite.CompositeTerminable
import net.evilblock.cubed.nametag.NametagInfo
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/23/2024
 */
interface MiniGameLifecycle<T : MiniGameConfiguration> : CompositeTerminable
{
    val configuration: T
    val typeConfiguration: MiniGameTypeMetadata

    val game: AbstractMiniGameGameImpl<T>
    val scoreboard: MiniGameScoreboard
    val events: List<MiniGameEvent>

    fun configure()
    fun provideNametagFor(viewed: Player, viewer: Player): NametagInfo?
    {
        return null
    }
}
