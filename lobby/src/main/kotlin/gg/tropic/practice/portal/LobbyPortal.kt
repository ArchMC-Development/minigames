package gg.tropic.practice.portal

import gg.scala.commons.agnostic.sync.ServerSync
import org.bukkit.Location
import java.util.UUID

data class LobbyPortal(
    val identifier: UUID = UUID.randomUUID(),
    var destination: String = "duels",
    var server: String = ServerSync.getLocalGameServer().groups.first(),
    val blocks: MutableList<Location> = mutableListOf()
)
