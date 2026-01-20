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
        serviceName: String = "arch-minigames"
    )
    {
        if (initialized) return

        Sentry.init { options: SentryOptions ->
            options.dsn = dsn
            options.environment = environment
            options.tracesSampleRate = 1.0
            options.isEnableTracing = true
            options.setTag("service", serviceName)
        }
        initialized = true
    }

    /**
     * Check if Sentry has been initialized.
     */
    @JvmStatic
    fun isInitialized(): Boolean = initialized

    /**
     * Capture an exception with additional context.
     *
     * @param throwable The exception to capture
     * @param context Additional context data as key-value pairs
     */
    @JvmStatic
    fun captureException(throwable: Throwable, context: Map<String, Any> = emptyMap())
    {
        Sentry.captureException(throwable) { scope ->
            context.forEach { (key, value) ->
                scope.setExtra(key, value)
            }
        }
    }

    /**
     * Add a breadcrumb for tracing user actions.
     *
     * @param category The category of the breadcrumb
     * @param message The message describing the action
     * @param data Additional data for context
     */
    @JvmStatic
    fun addBreadcrumb(category: String, message: String, data: Map<String, Any> = emptyMap())
    {
        Sentry.addBreadcrumb(message).apply {
            this.category = category
            data.forEach { (key, value) ->
                setData(key, value)
            }
        }
    }
}
