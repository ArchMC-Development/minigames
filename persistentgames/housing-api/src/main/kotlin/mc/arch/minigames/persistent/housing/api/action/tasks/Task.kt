package mc.arch.minigames.persistent.housing.api.action.tasks

import mc.arch.minigames.persistent.housing.api.action.option.TaskOption
import net.evilblock.cubed.serializers.Serializers
import java.util.UUID

abstract class Task(
    val id: String,
    val displayName: String,
    val options: MutableMap<String, TaskOption>,
)
{
    abstract fun apply(playerId: UUID)

    inline fun <reified V> option(id: String): V =
        Serializers.gson.fromJson(options[id]?.data ?: "Unknown", V::class.java)
}