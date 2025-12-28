package mc.arch.pubapi.pigdi.config

import mc.arch.pubapi.pigdi.security.ApiKeyAuthFilter
import mc.arch.pubapi.pigdi.security.RateLimitFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

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
) : WebMvcConfigurer
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

    override fun addResourceHandlers(registry: ResourceHandlerRegistry)
    {
        registry.addResourceHandler(
            "/favicon.ico",
            "/favicon-16x16.png",
            "/favicon-32x32.png",
            "/apple-touch-icon.png",
            "/android-chrome-192x192.png",
            "/android-chrome-512x512.png",
            "/site.webmanifest"
        ).addResourceLocations("classpath:/")
    }
}
