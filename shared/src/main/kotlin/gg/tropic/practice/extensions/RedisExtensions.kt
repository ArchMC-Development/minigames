package gg.tropic.practice.extensions

import io.lettuce.core.api.sync.RedisCommands
import net.evilblock.cubed.serializers.Serializers

/**
 * @author GrowlyX
 * @since 8/18/2024
 */
inline fun <reified T> RedisCommands<String, String>.getAs(field: String) = get(field)
    ?.let { Serializers.gson.fromJson(it, T::class.java) }

inline fun <reified T> String.into() = Serializers.gson.fromJson(this, T::class.java)
