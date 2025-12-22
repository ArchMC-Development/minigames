package gg.tropic.practice.privategames.settings

import net.evilblock.cubed.serializers.impl.AbstractTypeSerializable

/**
 * Base interface for private game settings.
 * Each minigame can define its own settings by implementing this interface.
 *
 * @author GrowlyX
 * @since 12/21/24
 */
interface PrivateGameSetting<T> : AbstractTypeSerializable
{
    /**
     * Unique identifier for this setting.
     */
    val id: String

    /**
     * Display name shown in settings menu.
     */
    val displayName: String

    /**
     * Description of what this setting does.
     */
    val description: List<String>

    /**
     * Default value for this setting.
     */
    val defaultValue: T

    /**
     * Current value of this setting.
     */
    var value: T

    /**
     * Available options for this setting (for toggle/cycle settings).
     */
    fun availableValues(): List<T> = listOf()

    /**
     * Cycle to the next value (for toggle/cycle settings).
     */
    fun cycleValue()
    {
        val options = availableValues()
        if (options.isEmpty()) return
        
        val currentIndex = options.indexOf(value)
        val nextIndex = (currentIndex + 1) % options.size
        value = options[nextIndex]
    }

    /**
     * Reset to default value.
     */
    fun reset()
    {
        value = defaultValue
    }
}
