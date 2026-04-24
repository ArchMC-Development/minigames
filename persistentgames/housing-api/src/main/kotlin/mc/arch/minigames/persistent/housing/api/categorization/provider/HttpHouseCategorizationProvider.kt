package mc.arch.minigames.persistent.housing.api.categorization.provider

import mc.arch.minigames.persistent.housing.api.categorization.CategorizationConfig
import mc.arch.minigames.persistent.housing.api.categorization.HouseCategorizationProvider
import mc.arch.minigames.persistent.housing.api.categorization.model.CategorizationRequest
import mc.arch.minigames.persistent.housing.api.categorization.model.CategorizationResult
import net.evilblock.cubed.serializers.Serializers
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture

/**
 * Synchronous-over-HTTP provider. Useful for dev/test and for low-volume
 * deployments where a single gateway pod fronts an in-process pipeline.
 *
 * For high throughput prefer [RedisStreamHouseCategorizationProvider] so the
 * three pipeline stages can scale independently (the whole point of the
 * App Store pipeline design).
 */
class HttpHouseCategorizationProvider(
    private val config: CategorizationConfig = CategorizationConfig.DEFAULT
) : HouseCategorizationProvider
{
    override val name: String = "http"

    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    override fun categorize(request: CategorizationRequest): CompletableFuture<CategorizationResult>
    {
        val body = Serializers.gson.toJson(request)
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("${config.httpBaseUrl.trimEnd('/')}/v1/categorize"))
            .timeout(Duration.ofMillis(config.resultTimeoutMillis))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
            .thenApply { response ->
                require(response.statusCode() in 200..299) {
                    "Categorization gateway returned ${response.statusCode()}: ${response.body()}"
                }
                Serializers.gson.fromJson(response.body(), CategorizationResult::class.java)
            }
    }
}
