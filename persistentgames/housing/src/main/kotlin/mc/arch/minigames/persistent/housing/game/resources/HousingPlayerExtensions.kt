package mc.arch.minigames.persistent.housing.game.resources

import gg.tropic.practice.ugc.toHostedWorldAs
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import mc.arch.minigames.persistent.housing.api.service.PlayerHousingService
import mc.arch.minigames.persistent.housing.game.instance.HousingHostedWorldInstance
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

fun Player.getPlayerHouseFromInstance(): CompletableFuture<PlayerHouse?> =
    this.toHostedWorldAs<HousingHostedWorldInstance>()?.let {
        PlayerHousingService.findById(it.globalId)
    } ?: CompletableFuture.completedFuture(null)