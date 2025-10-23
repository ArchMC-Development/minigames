package gg.tropic.practice.extensions

/**
 * @author Subham
 * @since 6/30/25
 */
fun Long.toShortString(): String
{
    return when
    {
        this >= 1_000_000_000 -> String.format("%.1fB", this / 1_000_000_000.0)
        this >= 1_000_000 -> String.format("%.1fM", this / 1_000_000.0)
        this >= 1_000 -> String.format("%.1fK", this / 1_000.0)
        else -> this.toString()
    }.replace(".0", "") // Remove .0 for whole numbers like 2.0K -> 2K
}
