package gg.tropic.practice.observability

import io.sentry.Sentry
import io.sentry.SentryOptions

/**
 * Centralized Sentry initialization and utilities for distributed tracing
 * across minigame RPC flows.
 *
 * @author Subham
 * @since 1/20/26
 */
object SentryIntegration
{
    private var initialized = false

    /**
     * Initialize Sentry with the provided DSN.
     * Safe to call multiple times - only the first call will initialize.
     *
     * @param dsn The Sentry DSN (Data Source Name)
     * @param environment The environment name (e.g., "production", "staging")
     * @param serviceName The service name for tagging (e.g., "queue-server", "minigame-plugin")
     */
    @JvmStatic
    fun initialize(
        dsn: String,
        environment: String = "production",
        serviceName: String = "app"
    )
    {
        if (initialized) return

        Sentry.init { options: SentryOptions ->
            options.dsn = dsn
            options.environment = environment
            options.tracesSampleRate = 1.0
            options.setTag("service", serviceName)
        }
        initialized = true
    }

    /**
     * Check if Sentry has been initialized.
     */
    @JvmStatic
    fun isInitialized(): Boolean = initialized
}
