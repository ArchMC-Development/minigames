package gg.tropic.practice.replacements

import gg.scala.commons.replacement.Replacement
import gg.scala.commons.replacement.ReplacementBuilder
import gg.scala.commons.replacement.annotation.ReplacementAutoRegister
import gg.scala.lemon.aggregate.ServerGroupAggregate
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 5/2/2022
 */
@ReplacementAutoRegister
object ServerGroupTextReplacement : Replacement
{
    private val cachedAggregates = mutableMapOf<String, ServerGroupAggregate>()

    override val replacements =
        ReplacementBuilder
            .newBuilder()
            .process("scoreboard-display") { _, id ->
                val server = this.aggregate(id)

                when (
                    server.prominentServerStatus ?: ServerGroupAggregate.ServerStatus.OFFLINE
                )
                {
                    ServerGroupAggregate.ServerStatus.WHITELISTED -> "${CC.RED}Whitelisted"
                    ServerGroupAggregate.ServerStatus.OFFLINE -> "${CC.RED}Offline"
                    ServerGroupAggregate.ServerStatus.ONLINE -> "${server.totalPlayerCount ?: 0}/${server.totalMaxPlayerCount}"
                }
            }
            .process("status") { _, id ->
                val server = this.aggregate(id)

                when (
                    server.prominentServerStatus ?: ServerGroupAggregate.ServerStatus.OFFLINE
                )
                {
                    ServerGroupAggregate.ServerStatus.WHITELISTED -> "${CC.RED}Servers whitelisted"
                    ServerGroupAggregate.ServerStatus.OFFLINE -> "${CC.RED}Servers offline"
                    ServerGroupAggregate.ServerStatus.ONLINE -> "${CC.GREEN}Servers online"
                }
            }
            .process("scoreboard-display2") { _, id ->
                val server = this.aggregate(id)

                when (
                    server.prominentServerStatus ?: ServerGroupAggregate.ServerStatus.OFFLINE
                )
                {
                    ServerGroupAggregate.ServerStatus.WHITELISTED -> "${CC.RED}Whitelisted"
                    ServerGroupAggregate.ServerStatus.OFFLINE -> "0"
                    ServerGroupAggregate.ServerStatus.ONLINE -> "${server.totalPlayerCount ?: 0}"
                }
            }
            .process("players") { _, id ->
                "${this.aggregate(id).totalPlayerCount ?: 0}"
            }
            .process("fancy") { _, id ->
                val playerCount = this.aggregate(id).totalPlayerCount ?: 0
                "$playerCount player${if (playerCount == 1) "" else "s"} playing..."
            }
            .process("servers") { _, id ->
                "${this.aggregate(id).totalServerCount ?: 0}"
            }
            .process("max-players") { _, id ->
                "${this.aggregate(id).totalMaxPlayerCount ?: 0}"
            }

    override fun id() = "group"

    private fun aggregate(id: String): ServerGroupAggregate
    {
        if (cachedAggregates.containsKey(id))
        {
            return cachedAggregates[id]!!
        }

        val aggregate = ServerGroupAggregate(id)
            .apply {
                subscribeAggregateRefresh()
            }

        this.cachedAggregates[id] = aggregate

        return aggregate
    }
}
