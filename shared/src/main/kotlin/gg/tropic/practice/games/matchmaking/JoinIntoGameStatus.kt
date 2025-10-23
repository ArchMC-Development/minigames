package gg.tropic.practice.games.matchmaking

enum class JoinIntoGameStatus
{
    SUCCESS,
    FAILED_NON_EMPTY_TEAMS,
    FAILED_RPC_FAILURE,
    FAILED_GAME_NOT_FOUND,
    FAILED_ALREADY_STARTED,
}
