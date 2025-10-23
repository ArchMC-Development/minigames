package gg.tropic.practice.games.robot

import gg.tropic.practice.games.GameImpl

/**
 * @author GrowlyX
 * @since 7/20/2024
 */
object RobotGameLifecycle
{
    var createNewRobotInstances: (GameImpl) -> Set<RobotInstance> = {
        throw UnsupportedOperationException("Robot integration not available")
    }
}
