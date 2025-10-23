package gg.tropic.practice.games.team

/**
 * @author GrowlyX
 * @since 8/9/2022
 */
data class TeamIdentifier(val label: String)
{
    companion object
    {
        val ID = mutableListOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P")
            .associateWith { TeamIdentifier(it) }

        @JvmStatic val A = ID["A"]!!
        @JvmStatic val B = ID["B"]!!
        @JvmStatic val C = ID["C"]!!
        @JvmStatic val D = ID["D"]!!
    }

    override fun equals(other: Any?): Boolean
    {
        if (other !is TeamIdentifier)
        {
            return false
        }

        return other.label == label
    }

    fun priority() = ID.keys.indexOf(label)
    fun nextOf() = ID.keys.indexOf(label)
        .let { ID.keys.toList().getOrNull(it + 1) }
        ?.let { ID[it] }

    override fun hashCode(): Int
    {
        return label.hashCode()
    }
}
