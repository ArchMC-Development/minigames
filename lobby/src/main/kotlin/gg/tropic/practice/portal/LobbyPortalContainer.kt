package gg.tropic.practice.portal

import java.util.UUID

data class LobbyPortalContainer(
    val portals: MutableMap<UUID, LobbyPortal> = mutableMapOf()
)
