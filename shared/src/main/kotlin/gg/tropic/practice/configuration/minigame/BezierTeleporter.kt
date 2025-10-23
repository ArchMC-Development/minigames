package gg.tropic.practice.configuration.minigame

import com.cryptomorin.xseries.XSound
import me.lucko.helper.Schedulers
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.pow
import kotlin.math.min

/**
 * A utility class for smooth teleportation between two points using Bézier curves
 * with a trapezoidal motion profile for natural acceleration and deceleration.
 */
object BezierTeleporter
{
    /**
     * Configuration for the trapezoidal motion profile
     */
    data class MotionProfile(
        val accelerationPhase: Double = 0.3,  // 30% of duration for acceleration
        val decelerationPhase: Double = 0.3,  // 30% of duration for deceleration
        val constantPhase: Double = 0.4       // 40% of duration for constant velocity
    ) {
        init {
            require(accelerationPhase + decelerationPhase + constantPhase == 1.0) {
                "Motion profile phases must sum to 1.0"
            }
        }
    }

    /**
     * Teleports a player from start to end location using a smooth Bézier curve
     * with trapezoidal motion profile.
     *
     * @param player The player to teleport
     * @param start Starting location
     * @param end Ending location
     * @param duration Duration of the teleportation in ticks (20 ticks = 1 second)
     * @param height Additional height for the curve arc (default: 5 blocks)
     * @param motionProfile Configuration for acceleration/deceleration phases
     * @param onComplete Callback executed when teleportation completes
     */
    fun teleportPlayer(
        player: Player,
        start: Location,
        end: Location,
        duration: Int = 60, // 3 seconds default
        height: Double = 5.0,
        motionProfile: MotionProfile = MotionProfile(),
        onComplete: (() -> Unit)? = null
    )
    {
        // Ensure both locations are in the same world
        if (start.world != end.world)
        {
            throw IllegalArgumentException("Start and end locations must be in the same world")
        }

        // Generate control points for cubic Bézier curve
        val controlPoints = generateBezierControlPoints(start, end, height)
        var previousPosition = start.toVector()
        var currentTick = 0
        var lastFlapTick = 0

        // Calculate phase boundaries
        val accelEndTick = (duration * motionProfile.accelerationPhase).toInt()
        val constantEndTick = (duration * (motionProfile.accelerationPhase + motionProfile.constantPhase)).toInt()

        // Schedule the smooth movement
        Schedulers.sync().runRepeating({ task ->
            if (!player.isOnline)
            {
                task.closeAndReportException()
                return@runRepeating
            }

            // Sound effect
            lastFlapTick += 1
            if (lastFlapTick == 5)
            {
                XSound.ENTITY_ENDER_DRAGON_FLAP.play(player)
                lastFlapTick = 0 // Fixed: was using == instead of =
            }

            if (currentTick >= duration)
            {
                onComplete?.invoke()
                task.closeAndReportException()
                return@runRepeating
            }

            // Calculate position along curve using trapezoidal motion profile
            val curveProgress = calculateTrapezoidalProgress(
                currentTick,
                duration,
                motionProfile,
                accelEndTick,
                constantEndTick
            )

            // Calculate current position on Bézier curve
            val currentPos = calculateBezierPosition(controlPoints, curveProgress)
            val velocity = currentPos.clone().subtract(previousPosition)

            // Apply velocity smoothing to prevent jarring movements
            val smoothedVelocity = smoothVelocity(velocity, currentTick, duration)
            player.velocity = smoothedVelocity

            previousPosition = currentPos.clone()
            currentTick++
        }, 0L, 1L)
    }

    /**
     * Calculates the progress along the curve using a trapezoidal motion profile.
     *
     * @param currentTick Current animation tick
     * @param totalDuration Total animation duration in ticks
     * @param profile Motion profile configuration
     * @param accelEndTick Tick where acceleration phase ends
     * @param constantEndTick Tick where constant velocity phase ends
     * @return Progress value between 0.0 and 1.0
     */
    private fun calculateTrapezoidalProgress(
        currentTick: Int,
        totalDuration: Int,
        profile: MotionProfile,
        accelEndTick: Int,
        constantEndTick: Int
    ): Double
    {
        val normalizedTime = currentTick.toDouble() / totalDuration.toDouble()

        return when {
            // Acceleration phase - quadratic ease-in
            currentTick <= accelEndTick -> {
                val accelProgress = currentTick.toDouble() / accelEndTick.toDouble()
                val accelContribution = profile.accelerationPhase * (accelProgress * accelProgress)
                accelContribution
            }

            // Constant velocity phase - linear
            currentTick <= constantEndTick -> {
                val constantProgress = (currentTick - accelEndTick).toDouble() / (constantEndTick - accelEndTick).toDouble()
                val constantContribution = profile.constantPhase * constantProgress
                profile.accelerationPhase + constantContribution
            }

            // Deceleration phase - quadratic ease-out
            else -> {
                val decelTicks = totalDuration - constantEndTick
                val decelProgress = (currentTick - constantEndTick).toDouble() / decelTicks.toDouble()
                val decelContribution = profile.decelerationPhase * (2 * decelProgress - decelProgress * decelProgress)
                profile.accelerationPhase + profile.constantPhase + decelContribution
            }
        }
    }

    /**
     * Applies velocity smoothing to prevent sudden direction changes.
     * Reduces velocity magnitude near the start and end of the animation.
     */
    private fun smoothVelocity(velocity: Vector, currentTick: Int, totalDuration: Int): Vector
    {
        val progress = currentTick.toDouble() / totalDuration.toDouble()

        // Apply smoothing factor that's lower at start and end
        val smoothingFactor = when {
            progress < 0.1 -> progress * 10.0 // Ramp up from 0 to 1 over first 10%
            progress > 0.9 -> (1.0 - progress) * 10.0 // Ramp down from 1 to 0 over last 10%
            else -> 1.0
        }

        return velocity.multiply(smoothingFactor)
    }

    /**
     * Generates control points for a cubic Bézier curve.
     * Creates an arc that goes up and then down between start and end points.
     */
    private fun generateBezierControlPoints(
        start: Location,
        end: Location,
        arcHeight: Double
    ): Array<Vector>
    {
        val startVec = start.toVector()
        val endVec = end.toVector()

        // Calculate midpoint and direction
        val midpoint = startVec.clone().add(endVec).multiply(0.5)
        val direction = endVec.clone().subtract(startVec).normalize()
        val distance = startVec.distance(endVec)

        // Create control points for a more natural arc
        val heightOffset = Vector(0.0, arcHeight, 0.0)

        // Adjust control points based on distance for better curve shape
        val controlOffset = min(distance * 0.25, 3.0)

        val p0 = startVec
        val p1 = startVec.clone()
            .add(direction.clone().multiply(controlOffset))
            .add(heightOffset.clone().multiply(0.7))
        val p2 = endVec.clone()
            .subtract(direction.clone().multiply(controlOffset))
            .add(heightOffset.clone().multiply(0.7))
        val p3 = endVec

        return arrayOf(p0, p1, p2, p3)
    }

    /**
     * Calculates a point on a cubic Bézier curve at parameter t (0 to 1).
     * Uses the standard cubic Bézier formula: B(t) = (1-t)³P₀ + 3(1-t)²tP₁ + 3(1-t)t²P₂ + t³P₃
     */
    private fun calculateBezierPosition(controlPoints: Array<Vector>, t: Double): Vector
    {
        val clampedT = t.coerceIn(0.0, 1.0)

        val p0 = controlPoints[0]
        val p1 = controlPoints[1]
        val p2 = controlPoints[2]
        val p3 = controlPoints[3]

        val oneMinusT = 1.0 - clampedT
        val oneMinusTSquared = oneMinusT.pow(2)
        val oneMinusTCubed = oneMinusT.pow(3)
        val tSquared = clampedT.pow(2)
        val tCubed = clampedT.pow(3)

        // Cubic Bézier formula
        return p0.clone().multiply(oneMinusTCubed)
            .add(p1.clone().multiply(3 * oneMinusTSquared * clampedT))
            .add(p2.clone().multiply(3 * oneMinusT * tSquared))
            .add(p3.clone().multiply(tCubed))
    }
}

/**
 * Extension function for velocity-based teleportation with trapezoidal motion profile
 */
fun Player.teleportWithVelocity(
    destination: Location,
    duration: Int = 60,
    height: Double = 5.0,
    motionProfile: BezierTeleporter.MotionProfile = BezierTeleporter.MotionProfile(),
    onComplete: (() -> Unit)? = null
) = BezierTeleporter.teleportPlayer(
    this, this.location, destination, duration, height, motionProfile, onComplete
)

/**
 * Extension function with preset motion profiles for common use cases
 */
fun Player.teleportWithVelocityPreset(
    destination: Location,
    duration: Int = 60,
    height: Double = 5.0,
    preset: MotionPreset = MotionPreset.BALANCED,
    onComplete: (() -> Unit)? = null
) {
    val profile = when (preset) {
        MotionPreset.QUICK_START -> BezierTeleporter.MotionProfile(0.2, 0.4, 0.4)
        MotionPreset.SMOOTH_LANDING -> BezierTeleporter.MotionProfile(0.4, 0.2, 0.4)
        MotionPreset.BALANCED -> BezierTeleporter.MotionProfile(0.3, 0.3, 0.4)
        MotionPreset.DRAMATIC -> BezierTeleporter.MotionProfile(0.5, 0.1, 0.4)
    }

    BezierTeleporter.teleportPlayer(this, this.location, destination, duration, height, profile, onComplete)
}

enum class MotionPreset {
    QUICK_START,    // Fast acceleration, longer deceleration
    SMOOTH_LANDING, // Longer acceleration, fast deceleration
    BALANCED,       // Equal acceleration and deceleration
    DRAMATIC        // Very long acceleration for dramatic effect
}
