package mc.arch.pubapi.pigdi.security

import com.google.gson.Gson
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mc.arch.pubapi.pigdi.dto.ErrorResponse
import mc.arch.pubapi.pigdi.service.AkersService
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter for validating API keys on protected endpoints.
 *
 * @author Subham
 * @since 12/27/24
 */
@Component
class ApiKeyAuthFilter(
    private val akersService: AkersService,
    private val gson: Gson
) : OncePerRequestFilter()
{
    companion object
    {
        const val API_KEY_HEADER = "X-API-KEY"
        const val OWNER_ID_ATTRIBUTE = "ownerId"
        const val API_KEY_ATTRIBUTE = "apiKey"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    )
    {
        // Skip auth for swagger and actuator endpoints
        val path = request.requestURI
        if (path.startsWith("/swagger") || 
            path.startsWith("/v3/api-docs") || 
            path.startsWith("/actuator") ||
            path.startsWith("/auth"))
        {
            filterChain.doFilter(request, response)
            return
        }

        val apiKey = request.getHeader(API_KEY_HEADER)

        if (apiKey.isNullOrBlank())
        {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Missing X-API-KEY header")
            return
        }

        // Validate API key format
        if (!apiKey.startsWith("amc-akers_") || apiKey.length != 22)
        {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_API_KEY", "Invalid API key format")
            return
        }

        // Validate API key
        val validation = akersService.validateApiKey(apiKey)

        if (!validation.valid)
        {
            sendError(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "INVALID_API_KEY",
                validation.reason ?: "API key validation failed"
            )
            return
        }

        // Store owner ID for downstream use
        request.setAttribute(OWNER_ID_ATTRIBUTE, validation.ownerId)
        request.setAttribute(API_KEY_ATTRIBUTE, apiKey)

        filterChain.doFilter(request, response)
    }

    private fun sendError(
        response: HttpServletResponse,
        status: Int,
        error: String,
        message: String
    )
    {
        response.status = status
        response.contentType = "application/json"
        response.writer.write(
            gson.toJson(
                ErrorResponse(
                    error = error,
                    message = message,
                    timestamp = System.currentTimeMillis()
                )
            )
        )
    }
}
