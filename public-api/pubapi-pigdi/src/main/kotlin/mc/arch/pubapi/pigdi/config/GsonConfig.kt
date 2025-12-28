package mc.arch.pubapi.pigdi.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Gson configuration for JSON serialization.
 *
 * @author Subham
 * @since 12/27/24
 */
@Configuration
class GsonConfig
{
    @Bean
    fun gson(): Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()
}
