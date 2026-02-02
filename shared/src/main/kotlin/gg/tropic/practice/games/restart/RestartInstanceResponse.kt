package gg.tropic.practice.games.restart

/**
 * Response from restart RPC.
 *
 * @author Subham
 * @since 1/22/26
 */
data class RestartInstanceResponse(
    val status: RestartStatus,
    val message: String? = null
)

enum class RestartStatus
{
    SUCCESS,
    ALREADY_DRAINING,
    SERVER_MISMATCH,
    FAILED
}
