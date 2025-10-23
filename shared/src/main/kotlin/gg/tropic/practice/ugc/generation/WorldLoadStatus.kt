package gg.tropic.practice.ugc.generation

/**
 * @author Subham
 * @since 7/18/25
 */
enum class WorldLoadStatus
{
    LOADED,
    FAILED_LOAD_WORLD,
    FAILED_NO_LOAD_RESPONSE,
    FAILED_NO_SERVERS_FOUND,
    FAILED_SERVER_DRAINING,
    FAILED_EMPTY_RESPONSE
}
