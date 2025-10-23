package gg.tropic.practice.resources

import gg.tropic.practice.games.GameService
import gg.tropic.practice.settings.isASilentSpectator
import gg.tropic.practice.ugc.toHostedWorld
import net.evilblock.cubed.visibility.VirtualVisibility
import net.evilblock.cubed.visibility.VisibilityAction
import net.evilblock.cubed.visibility.VisibilityAdapter
import net.evilblock.cubed.visibility.VisibilityAdapterRegister
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/9/2022
 */
@VisibilityAdapterRegister("duels")
object DuelsVisibilityImpl : VisibilityAdapter
{
    override fun getVirtualAppearanceBetween(viewer: Player, viewed: Player): VirtualVisibility
    {
        val hostedWorldPlayer = viewer.toHostedWorld()
        val hostedWorldTarget = viewed.toHostedWorld()

        val playerGame = GameService.byPlayerOrSpectator(viewer.uniqueId)
        val targetGame = GameService.byPlayerOrSpectator(viewed.uniqueId)

        val eligibleForSilentSpectator = !(playerGame != null && targetGame?.expectation == playerGame.expectation) ||
            playerGame.expectedSpectators.contains(viewed.uniqueId)

        if (
            (playerGame != null && targetGame?.expectation == playerGame.expectation && playerGame.miniGameLifecycle != null) || // Spectating in a minigame, and in the same minigame
            (hostedWorldPlayer != null && hostedWorldPlayer.globalId == hostedWorldTarget?.globalId) // In the same hosted world
        )
        {
            val isTargetSpectating = GameService.isSpectating(viewed)
            val isViewerSpectating = GameService.isSpectating(viewer)

            if (isTargetSpectating)
            {
                if (!isViewerSpectating) // Viewer is not spectating, but the target is spectating
                {
                    if (playerGame != null && playerGame.shouldShowAllPlayers)
                    {
                        if (viewed.isASilentSpectator() && eligibleForSilentSpectator)
                        {
                            return VirtualVisibility.HIDDEN
                        }

                        // In an event setting, all players may be visible
                        return VirtualVisibility.NEUTRAL
                    }

                    return VirtualVisibility.HIDDEN
                }

                if (viewed.isASilentSpectator() && eligibleForSilentSpectator) // Target is spectating
                {
                    return VirtualVisibility.HIDDEN
                }
            }

            return VirtualVisibility.NEUTRAL
        }

        return VirtualVisibility.NEUTRAL
    }

    override fun getAction(
        toRefresh: Player, refreshFor: Player
    ): VisibilityAction
    {
        val hostedWorldPlayer = refreshFor.toHostedWorld()
        val hostedWorldTarget = toRefresh.toHostedWorld()

        val playerGame = GameService.byPlayerOrSpectator(refreshFor.uniqueId)
        val targetGame = GameService.byPlayerOrSpectator(toRefresh.uniqueId)

        val eligibleForSilentSpectator = !(playerGame != null && targetGame?.expectation == playerGame.expectation) ||
            playerGame.expectedSpectators.contains(toRefresh.uniqueId)

        if (
            (playerGame != null && targetGame?.expectation == playerGame.expectation) ||
            (hostedWorldPlayer != null && hostedWorldPlayer.globalId == hostedWorldTarget?.globalId)
        )
        {
            val isTargetSpectating = GameService.isSpectating(toRefresh)
            val isViewerSpectating = GameService.isSpectating(refreshFor)

            if (isTargetSpectating)
            {
                if (!isViewerSpectating) // Viewer is not spectating, but the target is spectating
                {
                    if (playerGame != null && playerGame.shouldShowAllPlayers)
                    {
                        if (toRefresh.isASilentSpectator() && eligibleForSilentSpectator)
                        {
                            return VisibilityAction.HIDE
                        }

                        // In an event setting, all players may be visible
                        return VisibilityAction.NEUTRAL
                    }

                    return VisibilityAction.HIDE
                }

                if (toRefresh.isASilentSpectator() && eligibleForSilentSpectator)
                {
                    return VisibilityAction.HIDE
                }
            }

            return VisibilityAction.NEUTRAL
        }

        return VisibilityAction.HIDE
    }
}
