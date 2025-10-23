package gg.tropic.practice.scoreboard.configuration

import gg.scala.commons.graduation.Schoolmaster
import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.commons.persist.datasync.DataSyncSource
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Service
import gg.tropic.practice.PracticeLobby
import net.evilblock.cubed.util.CC

/**
 * Class created on 10/12/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
@Service
object LobbyScoreboardConfigurationService : DataSyncService<LobbyScoreboardConfig>()
{
    @Inject
    lateinit var plugin: PracticeLobby

    override fun keys() = LobbyScoreboardKeys
    override fun type() = LobbyScoreboardConfig::class.java

    override fun locatedIn() = DataSyncSource.Mongo

    object LobbyScoreboardKeys : DataSyncKeys
    {
        override fun store() = keyOf("lobby", "scoreboard-cfg")

        override fun newStore() = "lobby"
        override fun sync() = keyOf("lobby", "cfg-sync")
    }

    val schoolmaster = Schoolmaster<LobbyScoreboardConfig>().apply {
        stage("add-colors") {
            primaryColor = "${CC.PRI}"
            secondaryColor = "${CC.B_WHITE}"
        }
    }

    override fun postReload()
    {
        with(cached()) {
            if (schoolmaster.mature(this))
            {
                sync(this)
            }
        }
    }
}