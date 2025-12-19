package mc.arch.minigames.persistent.housing.game.resources

import gg.tropic.practice.ugc.toHostedWorldAs
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.api.service.PlayerHousingService
import mc.arch.minigames.persistent.housing.game.instance.HousingHostedWorldInstance
import org.bukkit.entity.Player

fun Player.getPlayerHouseFromInstance(): PlayerHouse? =
    this.toHostedWorldAs<HousingHostedWorldInstance>()?.let {
        PlayerHousingService.cached(it.globalId)
    }