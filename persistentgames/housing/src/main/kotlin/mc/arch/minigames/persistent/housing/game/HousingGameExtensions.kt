package mc.arch.minigames.persistent.housing.game

import gg.tropic.practice.persistence.RedisShared
import gg.tropic.practice.ugc.HostedWorldRPC
import gg.tropic.practice.ugc.PersistencePolicy
import gg.tropic.practice.ugc.WorldInstanceProviderType
import gg.tropic.practice.ugc.generation.visits.VisitWorldRequest
import gg.tropic.practice.ugc.generation.visits.VisitWorldStatus
import mc.arch.minigames.persistent.housing.api.VisitHouseConfiguration
import mc.arch.minigames.persistent.housing.api.content.HousingItemStack
import mc.arch.minigames.persistent.housing.api.model.PlayerHouse
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture

/**
 * Class created on 12/18/2025

 * @author Max C.
 * @project arch-minigames
 * @website https://solo.to/redis
 */
fun HousingItemStack.toBukkitStack(): ItemStack = ItemStack(Material.valueOf(this.material), this.amount)
    .also { itemStack ->
        val itemMeta = itemStack.itemMeta
            ?: Bukkit.getItemFactory().getItemMeta(itemStack.type)

        itemMeta.displayName = this.displayName
        itemMeta.lore = this.description

        itemStack.durability = this.data
        itemStack.itemMeta = itemMeta
    }

fun String.translateCC() = ChatColor.translateAlternateColorCodes('&', this)