package mc.arch.minigames.duelsmodern.lobby

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.filter.AbstractFilter

object MojangProfileLookupLogFilter : AbstractFilter()
{
    private const val SUPPRESSED_MESSAGE_PREFIX = "Couldn't look up profile properties for"
    private const val SUPPRESSED_EXCEPTION_FQN =
        "com.mojang.authlib.exceptions.MinecraftClientHttpException"

    /**
     * Installs this filter on the Log4j root logger. Idempotent — calling it
     * twice is harmless because Log4j keeps a list of filters and we register
     * the same singleton each time.
     */
    fun install()
    {
        val root = LogManager.getRootLogger() as? Logger ?: return
        // Don't double-register if something else (e.g. a /reload) re-runs
        // configure().
        if (root.filters?.asSequence()?.any { it === this } == true)
        {
            return
        }
        root.addFilter(this)
    }

    override fun filter(event: LogEvent?): Filter.Result
    {
        if (event == null) return Filter.Result.NEUTRAL

        val message = event.message?.formattedMessage
        if (message != null && message.startsWith(SUPPRESSED_MESSAGE_PREFIX))
        {
            return Filter.Result.DENY
        }

        // Belt-and-braces: also drop any log line whose attached throwable is
        // a MinecraftClientHttpException, in case Paper rephrases the message
        // in a future version.
        var thrown: Throwable? = event.thrown
        while (thrown != null)
        {
            if (thrown.javaClass.name == SUPPRESSED_EXCEPTION_FQN)
            {
                return Filter.Result.DENY
            }
            thrown = thrown.cause
        }

        return Filter.Result.NEUTRAL
    }
}
