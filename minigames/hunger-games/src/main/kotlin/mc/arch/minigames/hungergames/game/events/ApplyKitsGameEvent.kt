package mc.arch.minigames.hungergames.game.events

import gg.tropic.practice.minigame.MiniGameEvent
import mc.arch.minigames.hungergames.game.HungerGamesLifecycle
import mc.arch.minigames.hungergames.kits.HungerGamesKitDataSync
import mc.arch.minigames.hungergames.profile.HungerGamesProfileService
import net.evilblock.cubed.util.CC
import java.time.Duration

/**
 * @author ArchMC
 */
class ApplyKitsGameEvent(
    private val lifecycle: HungerGamesLifecycle,
    override val description: String = "Kits",
    override val duration: Duration = Duration.ofSeconds(60)
) : MiniGameEvent
{
    override fun execute()
    {
        val kitContainer = HungerGamesKitDataSync.cached()

        for (player in lifecycle.game.allNonSpectators())
        {
            val profile = HungerGamesProfileService.find(player) ?: continue
            val kitId = profile.selectedKit ?: continue
            val kit = kitContainer.kits[kitId] ?: continue

            kit.applyTo(player, profile.selectedKitLevel)
            player.sendMessage("${CC.GREEN}Your ${CC.GOLD}${kit.displayName}${CC.GREEN} kit has been applied!")
        }

        lifecycle.game.sendMessage("${CC.YELLOW}Kits have been distributed!")
    }
}
