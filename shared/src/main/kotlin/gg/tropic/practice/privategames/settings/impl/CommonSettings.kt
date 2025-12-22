package gg.tropic.practice.privategames.settings.impl

import gg.tropic.practice.privategames.settings.PrivateGameSetting

/**
 * Boolean toggle setting implementation.
 *
 * @author GrowlyX
 * @since 12/21/24
 */
class BooleanSetting(
    override val id: String,
    override val displayName: String,
    override val description: List<String>,
    override val defaultValue: Boolean = false
) : PrivateGameSetting<Boolean>
{
    override var value: Boolean = defaultValue

    override fun availableValues() = listOf(false, true)

    override fun getAbstractType() = BooleanSetting::class.java
}

/**
 * Integer setting with min/max bounds.
 *
 * @author GrowlyX
 * @since 12/21/24
 */
class IntSetting(
    override val id: String,
    override val displayName: String,
    override val description: List<String>,
    override val defaultValue: Int,
    val min: Int = 0,
    val max: Int = 100,
    val step: Int = 1
) : PrivateGameSetting<Int>
{
    override var value: Int = defaultValue

    override fun availableValues() = (min..max step step).toList()

    override fun getAbstractType() = IntSetting::class.java
}

/**
 * Double/multiplier setting with min/max bounds.
 *
 * @author GrowlyX
 * @since 12/21/24
 */
class DoubleSetting(
    override val id: String,
    override val displayName: String,
    override val description: List<String>,
    override val defaultValue: Double,
    val min: Double = 0.0,
    val max: Double = 10.0,
    val step: Double = 0.5
) : PrivateGameSetting<Double>
{
    override var value: Double = defaultValue

    override fun availableValues(): List<Double>
    {
        val values = mutableListOf<Double>()
        var current = min
        while (current <= max)
        {
            values.add(current)
            current += step
        }
        return values
    }

    override fun getAbstractType() = DoubleSetting::class.java
}

/**
 * Enum-based setting for predefined options.
 *
 * @author GrowlyX
 * @since 12/21/24
 */
class EnumSetting<E : Enum<E>>(
    override val id: String,
    override val displayName: String,
    override val description: List<String>,
    override val defaultValue: E,
    private val enumClass: Class<E>
) : PrivateGameSetting<E>
{
    override var value: E = defaultValue

    override fun availableValues(): List<E> = enumClass.enumConstants.toList()

    override fun getAbstractType() = EnumSetting::class.java
}
