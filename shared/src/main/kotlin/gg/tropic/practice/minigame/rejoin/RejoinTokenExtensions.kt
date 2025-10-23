package gg.tropic.practice.minigame.rejoin

import gg.scala.commons.ScalaCommons
import gg.tropic.practice.namespace
import net.evilblock.cubed.serializers.Serializers
import java.util.UUID

/**
 * @author Subham
 * @since 6/16/25
 */
fun UUID.toRejoinToken() = ScalaCommons
    .bundle()
    .globals()
    .redis()
    .sync()
    .get("${namespace()}:minigames:rejoin:$this")
    ?.let { text ->
        Serializers.gson.fromJson(text, RejoinToken::class.java)
    }

fun UUID.destroyRejoinToken() = ScalaCommons
    .bundle()
    .globals()
    .redis()
    .sync()
    .del("${namespace()}:minigames:rejoin:$this")
