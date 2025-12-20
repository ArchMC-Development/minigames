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

fun PlayerHouse.processVisitation(player: Player): CompletableFuture<Void>
{
    player.sendMessage("${CC.GRAY}Traveling to this realm...")

    return this.playerCanJoin(player.uniqueId).thenCompose {
        if (!it)
        {
            player.sendMessage("${CC.RED}You are now allowed to join this house!")
            return@thenCompose null
        }

        HostedWorldRPC.visitWorldRPCService
            .call(
                VisitWorldRequest(
                    visitingPlayers = setOfNotNull(player.uniqueId),
                    worldGlobalId = this.identifier,
                    configuration = VisitHouseConfiguration(
                        houseId = this.identifier,
                        persistencePolicy = PersistencePolicy.PERSISTENT
                    ),
                    ownerPlayerId = player.uniqueId,
                    providerType = WorldInstanceProviderType.REALM
                )
            )
            .thenAccept { response ->
                if (response.status == VisitWorldStatus.SUCCESS_REDIRECT)
                {
                    player.sendMessage("${CC.GREEN}You are being sent to this home!")
                    RedisShared.redirect(listOf(player.uniqueId), response.redirectToInstance!!)
                    return@thenAccept
                }

                player.sendMessage("${CC.RED}We weren't able to create a house for you. (${response.status})")
            }
            .exceptionally { throwable ->
                throwable.printStackTrace()
                player.sendMessage("${CC.RED}We weren't able to create a house for you right now. Try again later!")
                return@exceptionally null
            }
    }
}

fun String.translateCC() = ChatColor.translateAlternateColorCodes('&', this)