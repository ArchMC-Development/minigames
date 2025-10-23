package gg.tropic.practice.calendar

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Season(
    private val year: Int,
    private val month: Int
)
{
    constructor() : this(
        LocalDate.now().year,
        LocalDate.now().monthValue
    )

    // Format as "June 2025"
    fun formatLong(): String
    {
        val date = LocalDate.of(year, month, 1)
        return date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
    }

    // Format as "06-2025"
    fun formatShort(): String
    {
        return String.format("%02d-%d", month, year)
    }

    // Get next month
    fun nextMonth(): Season
    {
        val date = LocalDate.of(year, month, 1).plusMonths(1)
        return Season(date.year, date.monthValue)
    }

    // Get previous month
    fun lastMonth(): Season
    {
        val date = LocalDate.of(year, month, 1).minusMonths(1)
        return Season(date.year, date.monthValue)
    }

    override fun toString(): String = formatLong()
}
