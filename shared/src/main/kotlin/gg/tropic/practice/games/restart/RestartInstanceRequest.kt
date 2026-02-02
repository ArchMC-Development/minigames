package gg.tropic.practice.games.restart

/**
 * Request to trigger a server restart.
 * Sent from queue server to failing game instances.
 *
 * @author Subham
 * @since 1/22/26
 */
data class RestartInstanceRequest(
    val targetServer: String,
    val delaySeconds: Int = 60,
    val reason: String = "RPC failure threshold exceeded"
)
