package gg.tropic.practice.statistics

/**
 * @author Subham
 * @since 6/18/25
 */
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneOffset

fun getCurrentDateString(): String
{
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    return LocalDate.now().format(formatter)
}

fun getCurrentWeekYearString(): String
{
    val currentDate = LocalDate.now()
    val weekFields = WeekFields.of(Locale.getDefault())
    val weekNumber = currentDate.get(weekFields.weekOfWeekBasedYear())
    val year = currentDate.year
    return "$weekNumber-$year"
}

enum class StatisticLifetime(
    private val keyID: () -> String,
    private val timeUntilReset: () -> Long,
    private val timeAtReset: () -> Instant,
)
{
    Daily(
        keyID = { getCurrentDateString() },
        timeUntilReset = {
            val now = LocalDateTime.now()
            val endOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX)
            ChronoUnit.MILLIS.between(now, endOfDay)
        },
        timeAtReset = {
            val now = LocalDateTime.now()
            LocalDateTime
                .of(now.toLocalDate(), LocalTime.MAX)
                .toInstant(ZoneOffset.of("-6"))
        }
    ),
    Weekly(
        keyID = { getCurrentWeekYearString() },
        timeUntilReset = {
            val now = LocalDateTime.now()
            val endOfWeek = now.toLocalDate()
                .with(DayOfWeek.SUNDAY)
                .plusDays(if (now.dayOfWeek == DayOfWeek.SUNDAY) 0 else 1)
                .atTime(LocalTime.MAX)
            ChronoUnit.MILLIS.between(now, endOfWeek)
        },
        timeAtReset = {
            val now = LocalDateTime.now()
            val endOfWeek = now.toLocalDate()
                .with(DayOfWeek.SUNDAY)
                .plusDays(if (now.dayOfWeek == DayOfWeek.SUNDAY) 0 else 1)
                .atTime(LocalTime.MAX)

            endOfWeek.toInstant(ZoneOffset.of("-6"))
        }
    );

    fun transform(id: String) = "$id:${getKeyID()}"

    fun getKeyID(): String = keyID()
    fun getTimeUntilReset(): Long = timeUntilReset()
    fun getTimeAtReset(): Instant = timeAtReset()
}
