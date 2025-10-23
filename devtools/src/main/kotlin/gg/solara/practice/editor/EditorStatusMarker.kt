package gg.solara.practice.editor

import gg.scala.commons.ScalaCommons
import gg.tropic.practice.namespace

/**
 * @author Subham
 * @since 6/24/25
 */
object EditorStatusMarker
{
    fun markAsComplete(world: String) = ScalaCommons.bundle().globals()
        .redis()
        .sync()
        .sadd("${namespace()}:devtools:complete", world)

    fun markAsInComplete(world: String) = ScalaCommons.bundle().globals()
        .redis()
        .sync()
        .srem("${namespace()}:devtools:complete", world)

    fun isComplete(world: String) = ScalaCommons.bundle().globals()
        .redis()
        .sync()
        .sismember("${namespace()}:devtools:complete", world)
}
