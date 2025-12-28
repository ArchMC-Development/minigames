package mc.arch.pubapi.pigdi.config

import mc.arch.pubapi.pigdi.security.ApiKeyAuthFilter
import mc.arch.pubapi.pigdi.security.RateLimitFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

/**
 * Web configuration for filters and security.
 *
 * @author Subham
 * @since 12/27/24
 */
@Configuration
class WebConfig(
    private val apiKeyAuthFilter: ApiKeyAuthFilter,
    private val rateLimitFilter: RateLimitFilter
)
{
    @Bean
    fun apiKeyAuthFilterRegistration(): FilterRegistrationBean<ApiKeyAuthFilter>
    {
        val registration = FilterRegistrationBean<ApiKeyAuthFilter>()
        registration.filter = apiKeyAuthFilter
        registration.addUrlPatterns("/v1/*")
        registration.order = Ordered.HIGHEST_PRECEDENCE
        return registration
    }

    @Bean
    fun rateLimitFilterRegistration(): FilterRegistrationBean<RateLimitFilter>
    {
        val registration = FilterRegistrationBean<RateLimitFilter>()
        registration.filter = rateLimitFilter
        registration.addUrlPatterns("/v1/*")
        registration.order = Ordered.HIGHEST_PRECEDENCE + 1
        return registration
    }
}
