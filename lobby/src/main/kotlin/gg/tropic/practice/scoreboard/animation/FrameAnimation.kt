package gg.tropic.practice.scoreboard.animation

import gg.tropic.practice.scoreboard.configuration.LobbyScoreboardConfigurationService

/**
 * Class created on 10/12/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
object FrameAnimation : Runnable {

    private var stage: Int = 0
    private var lastStage: Long = System.currentTimeMillis()

    override fun run() {
        val config = LobbyScoreboardConfigurationService.cached()

        if (!config.titleAnimated) {
            return
        }

        if (stage >= config.titleFrames.size) {
            stage = 0
        }

        val stageAt = config.titleFrames[stage]

        if (System.currentTimeMillis() - lastStage >= stageAt.delay) {
            lastStage = System.currentTimeMillis()

            if (stage + 1 >= config.titleFrames.size) {
                stage = 0
            } else {
                stage++
            }
        }
    }

    @JvmStatic
    fun getCurrentTitle(): String {
        return "${LobbyScoreboardConfigurationService.cached().titleFrames[stage].text} "
    }

}