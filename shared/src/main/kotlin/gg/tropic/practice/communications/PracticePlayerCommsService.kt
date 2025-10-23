package gg.tropic.practice.communications

import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.tropic.practice.serializable.Message
import gg.tropic.practice.settings.DuelsSettingCategory
import gg.tropic.practice.suffixWhenDev
import me.lucko.helper.utils.Players
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*
import java.util.logging.Logger

/**
 * @author GrowlyX
 * @since 8/25/2024
 */
@Service
object PracticePlayerCommsService
{
    @Inject
    lateinit var audiences: BukkitAudiences

    private val aware by lazy {
        AwareBuilder
            .of<AwareMessage>("communications-lobbies".suffixWhenDev())
            .codec(AwareMessageCodec)
            .logger(Logger.getAnonymousLogger())
            .build()
    }

    fun createMessage(packet: String, vararg pairs: Pair<String, Any?>): AwareMessage =
        AwareMessage.of(packet, this.aware, *pairs)

    @Configure
    fun configure()
    {
        fun AwareMessage.usePlayer(use: Player.() -> Unit)
        {
            val players = retrieve<List<String>>("playerIDs")
            players.forEach {
                val bukkit = Bukkit
                    .getPlayer(
                        UUID.fromString(it)
                    )
                    ?: return@forEach

                use(bukkit)
            }
        }

        aware.listen("send-message") {
            val message = retrieve<String>("message").split("\n")

            usePlayer {
                for (component in message)
                {
                    sendMessage(
                        Color.translate(
                            component
                                .replace("{primary}", CC.PRI)
                                .replace("{secondary}", CC.SEC)
                        )
                    )
                }
            }
        }

        aware.listen("send-action-message") {
            val message = Serializers.gson.fromJson(
                retrieve<String>("message"),
                Message::class.java
            )

            message.components.onEach {
                it.value = it.value
                    .replace("{primary}", CC.PRI)
                    .replace("{secondary}", CC.SEC)
            }

            usePlayer {
                message.sendToPlayer(player)
            }
        }

        aware.listen("send-action-broadcast") {
            val message = Serializers.gson.fromJson(
                retrieve<String>("message"),
                Message::class.java
            )

            message.components.onEach {
                it.value = it.value
                    .replace("{primary}", CC.PRI)
                    .replace("{secondary}", CC.SEC)
            }

            Players.all().forEach(message::sendToPlayer)
        }

        aware.listen("send-notification-sound") {
            val setting = retrieve<String>("setting")

            usePlayer {
                val profile = BasicsProfileService.find(this)
                    ?: return@usePlayer

                if (profile.setting("${DuelsSettingCategory.DUEL_SETTING_PREFIX}$setting", StateSettingValue.DISABLED) == StateSettingValue.ENABLED)
                {
                    playSound(
                        location,
                        Sound.NOTE_PLING,
                        1.0f,
                        1.0f
                    )
                }
            }
        }

        aware.listen("redirect") {
            usePlayer {
                val audience = audiences.player(this)
                audience.sendActionBar(
                    Component.text("${CC.B_GREEN}MATCH FOUND!")
                )

                VelocityRedirectSystem.redirect(
                    this, retrieve("server")
                )
            }
        }
        aware.connect().toCompletableFuture().join()
    }
}
