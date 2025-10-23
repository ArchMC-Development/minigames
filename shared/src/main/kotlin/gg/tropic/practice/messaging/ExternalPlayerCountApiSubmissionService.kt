package gg.tropic.practice.messaging

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.ScalaCommons
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.tropic.practice.configuration.PracticeConfigurationService
import me.lucko.helper.Schedulers
import net.evilblock.cubed.ScalaCommonsSpigot
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Subham
 * @since 7/29/25
 */
@Service
object ExternalPlayerCountApiSubmissionService
{
    @Inject
    lateinit var plugin: ExtendedScalaPlugin

    private val legacyServerPlayerCounts = ConcurrentHashMap<String, Int>()
    private val legacyServers = mutableListOf(
        "legacy-creative",
        "legacy-survival",
        "legacy-lifesteal"
    )

    private lateinit var playerCountService: PlayerCountService
    private var globalPlayerCount = 0

    @Configure
    fun configure()
    {
        fun reconfigureClient()
        {
            playerCountService = Retrofit.Builder()
                .baseUrl(PracticeConfigurationService.cached().externalPlayerCountBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PlayerCountService::class.java)
        }

        reconfigureClient()
        PracticeConfigurationService.onReload {
            reconfigureClient()
        }

        Schedulers
            .async()
            .runRepeating({ _ ->
                legacyServers.forEach { legacy ->
                    legacyServerPlayerCounts[legacy] = kotlin.runCatching {
                        playerCountService.getPlayerCount(legacy).execute().body() ?: 0
                    }.getOrDefault(0)
                }

                runCatching {
                    playerCountService.setPlayerCount("global", globalPlayerCount).execute()
                }
                legacyServerPlayerCounts["global"] = globalPlayerCount
            }, 0L, 5L)
            .bindWith(plugin)

        Schedulers
            .async()
            .runRepeating(Runnable {
                globalPlayerCount = ScalaCommonsSpigot
                    .instance.kvConnection
                    .sync().hgetall(
                        "symphony:instances"
                    )
                    .filter { pair ->
                        System.currentTimeMillis() - (ScalaCommons.bundle().globals().redis()
                            .sync()
                            .hget("symphony:heartbeats", pair.key)
                            ?.toLongOrNull() ?: 0) < Duration
                            .ofSeconds(5L)
                            .toMillis()
                    }
                    .values
                    .sumOf { it.toIntOrNull() ?: 0 }
            }, 0L, 10L)
            .bindWith(plugin)
    }

    fun getLegacyPlayerCount(server: String) = legacyServerPlayerCounts[server] ?: 0
}
