package mc.arch.minigames.persistent.housing.api.content

import net.evilblock.cubed.util.CC

/**
 * Class created on 12/22/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
enum class HousingTime(val displayName: String)
{
    MORNING("${CC.GOLD}Morning"),
    NOON("${CC.YELLOW}Noon"),
    AFTERNOON("${CC.GREEN}Afternoon"),
    EVENING("${CC.PINK}Evening"),
    MIDNIGHT("${CC.D_PURPLE}Midnight")
}