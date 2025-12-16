package mc.arch.minigames.persistent.housing.api.model

enum class VisitationStatus(
    val description: String
)
{
    PUBLIC("Allows anyone to visit."),
    PARTY("Allows party members to visit."),
    GUILD("Allows guild members to visit."),
    FRIENDS("Allows friends to visit."),
    PRIVATE("Nobody can visit.")
}