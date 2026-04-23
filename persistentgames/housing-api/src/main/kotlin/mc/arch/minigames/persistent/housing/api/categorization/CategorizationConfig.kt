package mc.arch.minigames.persistent.housing.api.categorization

/**
 * Deployment-tunable settings. Both `http` and `redis` transports read from
 * the same config so an operator can flip the provider without a rebuild.
 */
data class CategorizationConfig(
    val httpBaseUrl: String = System.getenv("HOUSING_ML_URL") ?: "http://housing-ml-gateway:8080",
    val redisRequestStream: String = "housing:categorize:stage1",
    val redisResultKeyPrefix: String = "housing:categorize:result:",
    val pipelineVersion: String = "v1",
    val resultTimeoutMillis: Long = 15_000L,
    val resultPollIntervalMillis: Long = 150L
)
{
    companion object
    {
        val DEFAULT = CategorizationConfig()
    }
}
