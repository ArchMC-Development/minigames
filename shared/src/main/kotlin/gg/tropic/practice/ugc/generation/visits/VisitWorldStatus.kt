package gg.tropic.practice.ugc.generation.visits

/**
 * @author Subham
 * @since 7/20/25
 */
enum class VisitWorldStatus
{
    FAILED_RPC_FAILURE,
    FAILED_NO_SERVERS_AVAILABLE,
    FAILED_WORLD_GENERATION_FAILURE,
    FAILED_UNAVAILABLE_INSTANCE_DRAINING,
    SUCCESS_REDIRECT
}
