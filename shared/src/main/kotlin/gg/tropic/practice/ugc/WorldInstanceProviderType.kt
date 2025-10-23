package gg.tropic.practice.ugc

import com.cryptomorin.xseries.XMaterial
import net.evilblock.cubed.util.bukkit.ItemUtils

/**
 * @author Subham
 * @since 7/18/25
 */
enum class WorldInstanceProviderType(
    val playerHeadValue: String,
    val lobbyGroup: String
)
{
    SKYBLOCK(
        lobbyGroup = "skyblock-lobby",
        playerHeadValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA0ODMxZjdhN2Q4ZjYyNGM5NjMzOTk2ZTM3OThlZGFkNDlhNWQ5YmNkMThlY2Y3NWJmYWU2NmJlNDhhMGE2YiJ9fX0="
    ),
    REALM(
        lobbyGroup = "realms-lobby",
        playerHeadValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTdmODJhY2ViOThmZTA2OWU4YzE2NmNlZDAwMjQyYTc2NjYwYmJlMDcwOTFjOTJjZGRlNTRjNmVkMTBkY2ZmOSJ9fX0="
    ),
    TEMPORARY_WORLD(
        lobbyGroup = "hub",
        playerHeadValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjgxZWYyM2VkN2RhN2VlMDQ4YThjNTk5OGEwMWUwZDNkNTM1N2Q0MjZhMThhYzllOTYxM2E1ZGQ1MzMzZWJkNCJ9fX0="
    ),
    BRIDGING_PRACTICE(
        lobbyGroup = "hub",
        playerHeadValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDIxZTc2YzZkODE2MzZiZmY2ZTU3Yjc4NTk3MjU1OWIwOWUzOTU3NGE4ZDYzMGRhYmE3YTYyNDk5NmEzZmQ2OCJ9fX0="
    ),
    PRISON(
        lobbyGroup = "prison-lobby",
        playerHeadValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI3Zjk1NWNiNDdjNGYwMGZmYjE0N2FiMDhhNjU0ZGE3OWMxYWNmODQ3ODAzMDljZmNjOWJkMWU1MzUyYWIwNiJ9fX0="
    );

    fun toPlayerHead() = ItemUtils
        .applySkullTexture(
            XMaterial.PLAYER_HEAD.parseItem()!!,
            playerHeadValue
        )
}
