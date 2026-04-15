package mc.arch.minigames.hungergames.game

import com.cryptomorin.xseries.XMaterial
import com.cryptomorin.xseries.XSound
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.util.QuickAccess
import gg.tropic.game.extensions.economy.Accounts
import gg.tropic.game.extensions.economy.Transaction
import gg.tropic.game.extensions.economy.TransactionService
import gg.tropic.game.extensions.economy.TransactionType
import gg.tropic.practice.expectation.ExpectationService.plugin
import gg.tropic.practice.expectation.ExpectationService.returnToSpawnItem
import gg.tropic.practice.games.GameRemovalEvent
import gg.tropic.practice.games.GameService
import gg.tropic.practice.games.GameService.gracefullyRemoveFromGame
import gg.tropic.practice.games.GameState
import gg.tropic.practice.games.damage.DeathMessageStrategy
import gg.tropic.practice.games.damage.EliminationCause
import gg.tropic.practice.games.event.GameCompleteEvent
import gg.tropic.practice.games.event.PlayerJoinGameEvent
import gg.tropic.practice.games.event.PlayerSelectSpawnLocationEvent
import gg.tropic.practice.minigame.AbstractMiniGameGameImpl
import gg.tropic.practice.minigame.BasicMiniGameOrchestrator
import gg.tropic.practice.minigame.broadcastSeconds
import gg.tropic.practice.minigame.event.MiniGameStartTickEvent
import gg.tropic.practice.minigame.event.PlayerMiniGameDisconnectMidGameEvent
import gg.tropic.practice.minigame.event.functionality.MiniGamePlayerDeathEvent
import gg.tropic.practice.statistics.StatisticService
import gg.tropic.practice.strategies.MarkSpectatorStrategy
import mc.arch.minigames.hungergames.HungerGamesGameConfiguration
import mc.arch.minigames.hungergames.HungerGamesTypeMetadata
import mc.arch.minigames.hungergames.kits.menu.HungerGamesSelectKitMenu
import mc.arch.minigames.hungergames.statistics.CoreHungerGamesStatistic
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EnchantingInventory
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * @author ArchMC
 */
@Service
object HungerGamesGameOrchestrator : BasicMiniGameOrchestrator<HungerGamesGameConfiguration>()
{
    override val id = "hungergames"
    @Inject
    lateinit var audiences: BukkitAudiences

    override fun prepare(
        miniGame: AbstractMiniGameGameImpl<HungerGamesGameConfiguration>,
        configuration: HungerGamesGameConfiguration
    ) = HungerGamesLifecycle(configuration, miniGame)

    @Configure
    fun configure()
    {
        val selectAKitItem = ItemBuilder.of(XMaterial.BOW)
            .name("${CC.GREEN}Select a Kit ${CC.GRAY}(Right Click)")
            .addToLore(
                "${CC.GRAY}Use this bow to select a hunger games kit!"
            )
            .build()

        // Countdown tick
        Events
            .subscribe(MiniGameStartTickEvent::class.java)
            .filter { it.game.miniGameLifecycle is HungerGamesLifecycle }
            .filter { it.secondsLeftUntilStart in broadcastSeconds }
            .handler {
                it.game.playSound(XSound.BLOCK_NOTE_BLOCK_HAT.parseSound()!!, 1.0f)
                it.game.sendMessage(
                    "${CC.YELLOW}Game starts in ${CC.RED}${it.secondsLeftUntilStart}${CC.YELLOW} second${
                        if (it.secondsLeftUntilStart == 1) "" else "s"
                    }!"
                )
            }

        // Auto-lapis enchanting tables
        Events
            .subscribe(InventoryOpenEvent::class.java)
            .filter {
                it.inventory is EnchantingInventory &&
                    GameService.byPlayer(it.player as Player)
                        ?.miniGameLifecycle is HungerGamesLifecycle
            }
            .handler {
                it.inventory.setItem(1, ItemBuilder
                    .of(XMaterial.LAPIS_LAZULI)
                    .name("${CC.BLUE}Lapis Lazuli")
                    .addToLore(
                        "${CC.GRAY}This enchanting table",
                        "${CC.GRAY}is auto-equipped with",
                        "${CC.GRAY}Lapis Lazuli!"
                    )
                    .amount(3)
                    .build())
            }

        Events
            .subscribe(InventoryCloseEvent::class.java)
            .filter {
                it.inventory is EnchantingInventory &&
                    GameService.byPlayer(it.player as Player)
                        ?.miniGameLifecycle is HungerGamesLifecycle
            }
            .handler {
                it.inventory.setItem(1, null)
            }

        Events
            .subscribe(InventoryClickEvent::class.java)
            .filter {
                it.inventory is EnchantingInventory &&
                    it.slot == 1 &&
                    GameService.byPlayer(it.whoClicked as Player)
                        ?.miniGameLifecycle is HungerGamesLifecycle
            }
            .handler {
                it.isCancelled = true
            }

        // Give lobby items on join
        Events
            .subscribe(PlayerJoinGameEvent::class.java)
            .filter { it.game.miniGameLifecycle is HungerGamesLifecycle }
            .filter { it.game.state(GameState.Waiting) || it.game.state(GameState.Starting) }
            .handler {
                it.player.inventory.setItem(0, selectAKitItem)
                it.player.inventory.setItem(8, returnToSpawnItem)
                it.player.updateInventory()
            }

        // Kit selection menu
        Events
            .subscribe(PlayerInteractEvent::class.java)
            .filter {
                it.hasItem() &&
                    (it.action == Action.RIGHT_CLICK_BLOCK || it.action == Action.RIGHT_CLICK_AIR) &&
                    it.item.isSimilar(selectAKitItem)
            }
            .handler {
                val game = GameService
                    .byPlayerOrSpectator(it.player.uniqueId)
                    ?: return@handler

                if (game.state(GameState.Starting) || game.state(GameState.Waiting))
                {
                    HungerGamesSelectKitMenu().openMenu(it.player)
                }
            }
            .bindWith(plugin)

        // Spawn location handling
        Events
            .subscribe(PlayerSelectSpawnLocationEvent::class.java)
            .filter { it.game.miniGameLifecycle is HungerGamesLifecycle }
            .filter { !it.spectator }
            .filter { it.game.state(GameState.Waiting) || it.game.state(GameState.Starting) }
            .handler {
                // SG doesn't use cages, players just stand on the pedestals
                it.player.teleport(it.location)
            }

        // Player quit - handle pre-game
        Events
            .subscribe(PlayerQuitEvent::class.java)
            .handler {
                val game = GameService.byPlayer(it.player)
                    ?: return@handler

                if (game.miniGameLifecycle !is HungerGamesLifecycle)
                {
                    return@handler
                }
            }

        // Prevent compass dropping
        Events
            .subscribe(PlayerDropItemEvent::class.java)
            .filter {
                it.itemDrop.itemStack.type == XMaterial.COMPASS.parseMaterial() &&
                    GameService.byPlayer(it.player)
                        ?.miniGameLifecycle is HungerGamesLifecycle
            }
            .handler {
                it.isCancelled = true
            }

        // Food level control - prevent hunger during pre-game
        Events
            .subscribe(FoodLevelChangeEvent::class.java)
            .filter {
                it.entity is Player &&
                    GameService.byPlayer(it.entity as Player)
                        ?.miniGameLifecycle is HungerGamesLifecycle
            }
            .handler {
                val game = GameService.byPlayer(it.entity as Player) ?: return@handler
                if (game.state(GameState.Waiting) || game.state(GameState.Starting))
                {
                    it.isCancelled = true
                    (it.entity as Player).foodLevel = 20
                }
            }

        // Block burn/fade prevention
        Events
            .subscribe(BlockBurnEvent::class.java)
            .filter {
                GameService.byWorld(it.block.world)
                    ?.miniGameLifecycle is HungerGamesLifecycle
            }
            .handler { it.isCancelled = true }

        Events
            .subscribe(BlockFadeEvent::class.java)
            .filter {
                GameService.byWorld(it.block.world)
                    ?.miniGameLifecycle is HungerGamesLifecycle
            }
            .handler { it.isCancelled = true }

        // Game complete - report, stats, rewards
        Events
            .subscribe(GameCompleteEvent::class.java)
            .filter { event -> event.game.miniGameLifecycle is HungerGamesLifecycle }
            .handler { event ->
                val hgGame = event.game.miniGameLifecycle as HungerGamesLifecycle
                val winners = event.game.report!!.winners
                    .mapNotNull { uniqueId ->
                        hgGame.playerResources[uniqueId]
                    }

                val topKillers = hgGame.playerResources.values
                    .filterNot { it.expectedSpectator }
                    .sortedByDescending { it.kills }
                    .take(3)

                event.game.sendCenteredMessage(
                    "${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(53)}",
                    "${CC.B_RED}GAME REPORT",
                    "${CC.YELLOW}Winners: ${
                        winners.joinToString("${CC.GRAY}, ${CC.WHITE}") { it.preferredPrefixedName }
                    }",
                    "",
                    "${CC.YELLOW}Top Killers:",
                    *topKillers
                        .mapIndexed { index, resources -> "${CC.B_WHITE}${index + 1}. ${resources.preferredPrefixedName} ${CC.GRAY}- ${CC.WHITE}${resources.kills}" }
                        .toTypedArray(),
                    "${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(53)}",
                )

                winners.forEach {
                    it.toPlayer()?.apply {
                        MarkSpectatorStrategy.sendWinPlayAgain(this)
                    }
                }

                val mode = HungerGamesTypeMetadata.gameModes[hgGame.configuration.mode.name.lowercase()]
                    ?: return@handler

                // Winner rewards
                winners.forEach { resources ->
                    TransactionService
                        .submit(
                            Transaction(
                                sender = Accounts.SERVER,
                                receiver = resources.player,
                                type = TransactionType.Deposit,
                                economy = "experience",
                                amount = 50L
                            )
                        )

                    TransactionService
                        .submit(
                            Transaction(
                                sender = Accounts.SERVER,
                                receiver = resources.player,
                                type = TransactionType.Deposit,
                                economy = "coins",
                                amount = 150L
                            )
                        )

                    resources.toPlayer()?.sendMessage("${CC.GOLD}+150 Coins (Winning a game)")
                    resources.toPlayer()?.sendMessage("${CC.GREEN}+50 Experience (Winning a game)")
                }

                // Statistics for all players
                hgGame.playerResources.forEach { (player, resources) ->
                    StatisticService
                        .update(player) {
                            if (player in winners.map { r -> r.player })
                            {
                                listOf(
                                    CoreHungerGamesStatistic.PLAYS.toCore(),
                                    CoreHungerGamesStatistic.PLAYS.toMode(mode),
                                    CoreHungerGamesStatistic.WINS.toCore(),
                                    CoreHungerGamesStatistic.WINS.toMode(mode),
                                    CoreHungerGamesStatistic.DAILY_WINS.toCore(),
                                    CoreHungerGamesStatistic.DAILY_WINS.toMode(mode),
                                    CoreHungerGamesStatistic.WIN_STREAK.toCore(),
                                    CoreHungerGamesStatistic.WIN_STREAK.toMode(mode),
                                ).forEach { statistic ->
                                    StatisticService.statistic(statistic) {
                                        add(1)
                                    }
                                }
                            } else
                            {
                                listOf(
                                    CoreHungerGamesStatistic.PLAYS.toCore(),
                                    CoreHungerGamesStatistic.PLAYS.toMode(mode),
                                    CoreHungerGamesStatistic.LOSSES.toCore(),
                                    CoreHungerGamesStatistic.LOSSES.toMode(mode),
                                    CoreHungerGamesStatistic.WIN_STREAK.toCore(),
                                    CoreHungerGamesStatistic.WIN_STREAK.toMode(mode)
                                ).forEach { statistic ->
                                    StatisticService.statistic(statistic) {
                                        update(0)
                                    }
                                }
                            }

                            listOf(
                                CoreHungerGamesStatistic.KILLS.toCore(),
                                CoreHungerGamesStatistic.KILLS.toMode(mode),
                                CoreHungerGamesStatistic.DAILY_KILLS.toCore(),
                                CoreHungerGamesStatistic.DAILY_KILLS.toMode(mode)
                            ).forEach { statistic ->
                                StatisticService.statistic(statistic) {
                                    add(resources.kills.toLong())
                                }
                            }

                            listOf(
                                CoreHungerGamesStatistic.DEATHS.toCore(),
                                CoreHungerGamesStatistic.DEATHS.toMode(mode),
                            ).forEach { statistic ->
                                StatisticService.statistic(statistic) {
                                    add(resources.deaths.toLong())
                                }
                            }

                            listOf(
                                CoreHungerGamesStatistic.ASSISTS.toCore(),
                                CoreHungerGamesStatistic.ASSISTS.toMode(mode),
                            ).forEach { statistic ->
                                StatisticService.statistic(statistic) {
                                    add(resources.assists.toLong())
                                }
                            }
                        }
                }
            }

        // Player disconnect mid-game
        Events
            .subscribe(PlayerMiniGameDisconnectMidGameEvent::class.java)
            .filter { it.game.miniGameLifecycle is HungerGamesLifecycle }
            .handler { event ->
                if (GameService.isSpectating(event.player))
                {
                    return@handler
                }

                event.player.gracefullyRemoveFromGame(
                    event = GameRemovalEvent(
                        drops = (event.player.inventory.contents.filterNotNull() + event.player.inventory.armorContents.filterNotNull()).toMutableList(),
                        shouldRespawn = false,
                        volatileState = false,
                    ),
                    cause = EliminationCause.UNKNOWN
                )

                event.game.sendMessage("${
                    QuickAccess.coloredName(event.player)
                }${CC.GRAY} disconnected.")
            }

        // Player death
        Events
            .subscribe(MiniGamePlayerDeathEvent::class.java)
            .filter { event -> event.game is HungerGamesLifecycle }
            .handler { event ->
                val killer = if ((event.killer as? Player)?.isOnline == true)
                    (event.killer as? Player) else null

                val hgGame = event.game as HungerGamesLifecycle
                val deathMessage = DeathMessageStrategy.generate(
                    killedBy = killer,
                    killedDisplayName = QuickAccess.coloredName(event.player)
                        ?: event.player.name,
                    killedByDisplayName = if (killer != null)
                        QuickAccess.coloredName(killer) else null,
                    eliminationCause = event.eliminationCause
                )

                MarkSpectatorStrategy.markSpectator(
                    player = event.player,
                    shouldAnnounce = false,
                    shouldAddSpectatorItem = true
                )

                Schedulers
                    .sync()
                    .runLater({
                        if (!event.player.isOnline)
                        {
                            return@runLater
                        }

                        val playerAudiences = audiences.player(event.player)
                        playerAudiences.showTitle(
                            Title.title(
                                Component.text("${CC.B_RED}YOU DIED!"),
                                Component.text("${CC.GRAY}Better luck next time!"),
                            )
                        )

                        event.player.teleport(hgGame.game.spectatorLocation())
                    }, 1L)

                val playerResources = hgGame.playerResourcesOf(event.player)
                playerResources.deaths += 1

                if (killer != null)
                {
                    val resources = hgGame.playerResourcesOf(killer)
                    resources.kills += 1

                    // Kill reward
                    TransactionService
                        .submit(
                            Transaction(
                                sender = Accounts.SERVER,
                                receiver = killer.uniqueId,
                                type = TransactionType.Deposit,
                                economy = "experience",
                                amount = 15L
                            )
                        )

                    TransactionService
                        .submit(
                            Transaction(
                                sender = Accounts.SERVER,
                                receiver = killer.uniqueId,
                                type = TransactionType.Deposit,
                                economy = "coins",
                                amount = 25L
                            )
                        )

                    killer.sendMessage("${CC.GOLD}+25 Coins (Eliminating a player)")
                    killer.sendMessage("${CC.GREEN}+15 Experience (Eliminating a player)")

                    GameService.runFinalDeathEffectsFor(
                        killer = killer,
                        killed = event.player,
                        game = hgGame.game,
                        invasive = false
                    )

                    // Regen on kill
                    killer.addPotionEffect(PotionEffect(
                        PotionEffectType.REGENERATION,
                        8 * 20,
                        0,
                        true,
                        true
                    ))
                }
            }
    }
}
