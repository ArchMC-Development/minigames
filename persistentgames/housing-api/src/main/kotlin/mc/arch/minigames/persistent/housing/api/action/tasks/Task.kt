package mc.arch.minigames.persistent.housing.api.action.tasks

import mc.arch.minigames.persistent.housing.api.action.option.TaskOption
import mc.arch.minigames.persistent.housing.api.action.player.ActionEvent
import net.evilblock.cubed.serializers.Serializers
import java.util.UUID

abstract class Task(
    val id: String,
    val displayName: String,
    val options: MutableMap<String, TaskOption>,
    val scopes: List<String> = listOf()
)
{
    abstract fun <E> apply(playerId: UUID?, event: E)

    fun appliesToEvent(event: ActionEvent) = scopes.isEmpty() || scopes.contains(event.id())

    fun <V> option(id: String): V =
        (options[id]?.data ?: "Unknown") as V
}