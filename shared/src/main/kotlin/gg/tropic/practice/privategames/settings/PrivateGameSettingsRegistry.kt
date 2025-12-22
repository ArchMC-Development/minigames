package gg.tropic.practice.privategames.settings

/**
 * Registry for game-specific private game settings.
 * Minigames register their available settings here.
 *
 * @author GrowlyX
 * @since 12/21/24
 */
object PrivateGameSettingsRegistry
{
    private val settingsByGame = mutableMapOf<String, MutableList<() -> PrivateGameSetting<*>>>()

    /**
     * Register a setting factory for a specific minigame type.
     * Factory is used so each game instance gets fresh settings.
     */
    fun register(gameType: String, settingFactory: () -> PrivateGameSetting<*>)
    {
        settingsByGame.getOrPut(gameType) { mutableListOf() }.add(settingFactory)
    }

    /**
     * Get fresh instances of all settings for a game type.
     */
    fun getSettingsFor(gameType: String): List<PrivateGameSetting<*>>
    {
        return settingsByGame[gameType]?.map { it() } ?: emptyList()
    }

    /**
     * Get all registered game types.
     */
    fun registeredGameTypes(): Set<String> = settingsByGame.keys
}
