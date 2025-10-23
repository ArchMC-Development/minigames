package gg.tropic.practice.ugc

import org.bukkit.entity.Player

/**
 * @author Subham
 * @since 7/20/25
 */
fun Player.toHostedWorld() = HostedWorldInstanceService.instanceOf(this)
inline fun <reified T : HostedWorldInstance<*>> Player.toHostedWorldAs() = HostedWorldInstanceService.instanceOf(this)
    ?.let {
        if (it is T)
        {
            return@let it
        }

        return@let null
    }
