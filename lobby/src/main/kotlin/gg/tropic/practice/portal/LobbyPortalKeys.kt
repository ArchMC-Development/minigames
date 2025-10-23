package gg.tropic.practice.portal

import gg.scala.commons.persist.datasync.DataSyncKeys

object LobbyPortalKeys : DataSyncKeys
{
    override fun store() = keyOf("hub-portals", "hub-portals")

    override fun newStore() = "hub-portals"
    override fun sync() = keyOf("hub-portals", "hub-portals-sync")
}
