package mc.arch.pubapi.pigdi.controller

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mc.arch.pubapi.pigdi.entity.AkersProfileDocument
import mc.arch.pubapi.pigdi.repository.AkersProfileRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.servlet.view.RedirectView
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Controller for Discord OAuth flow.
 *
 * @author Subham
 * @since 12/27/24
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Discord OAuth endpoints")
class DiscordOAuthController(
    private val redisTemplate: StringRedisTemplate,
    private val akersProfileRepository: AkersProfileRepository,
    private val gson: Gson
)
{
    @Value("\${discord.client-id:}")
    private lateinit var clientId: String

    @Value("\${discord.client-secret:}")
    private lateinit var clientSecret: String

    @Value("\${discord.redirect-uri:https://api.arch.mc/auth/discord/callback}")
    private lateinit var redirectUri: String

    private val restTemplate = RestTemplate()

    companion object
    {
        const val DISCORD_AUTHORIZE_URL = "https://discord.com/api/oauth2/authorize"
        const val DISCORD_TOKEN_URL = "https://discord.com/api/oauth2/token"
        const val DISCORD_USER_URL = "https://discord.com/api/users/@me"
        const val SCOPES = "identify"

        const val OAUTH_TOKEN_PREFIX = "akers:oauth:"
        const val OAUTH_TOKEN_TTL_SECONDS = 300L // 5 minutes
    }

    @GetMapping("/discord")
    @Operation(
        summary = "Initiate Discord OAuth flow",
        description = "Redirects to Discord for authorization"
    )
    fun initiateOAuth(
        @RequestParam token: UUID
    ): ResponseEntity<Any>
    {
        // Validate that the token exists in Redis
        val tokenKey = "$OAUTH_TOKEN_PREFIX$token"
        val tokenData = redisTemplate.opsForValue().get(tokenKey)

        if (tokenData == null)
        {
            return ResponseEntity.badRequest().body(buildErrorPage(
                "Invalid or Expired Link",
                "This OAuth link has expired or is invalid. Please run /api link again in-game."
            ))
        }

        // Build Discord OAuth URL
        val params = mutableMapOf(
            "client_id" to clientId,
            "redirect_uri" to redirectUri,
            "response_type" to "code",
            "scope" to SCOPES,
            "state" to token.toString()
        )

        val queryString = params.entries.joinToString("&") { (k, v) ->
            "$k=${java.net.URLEncoder.encode(v, "UTF-8")}"
        }

        return ResponseEntity.status(302)
            .header("Location", "$DISCORD_AUTHORIZE_URL?$queryString")
            .build()
    }

    @GetMapping("/discord/callback")
    @Operation(
        summary = "Handle Discord OAuth callback",
        description = "Exchanges authorization code for access token and links Discord account"
    )
    fun handleCallback(
        @RequestParam code: String,
        @RequestParam state: String
    ): ResponseEntity<String>
    {
        try
        {
            // 1. Validate state token from Redis
            val tokenKey = "$OAUTH_TOKEN_PREFIX$state"
            val tokenDataJson = redisTemplate.opsForValue().get(tokenKey)

            if (tokenDataJson == null)
            {
                return ResponseEntity.ok(buildErrorPage(
                    "Session Expired",
                    "Your linking session has expired. Please run /api link again in-game."
                ))
            }

            val tokenData = gson.fromJson(tokenDataJson, OAuthTokenData::class.java)
            val minecraftUuid = tokenData.minecraftUuid

            // 2. Exchange code for access token
            val accessToken = exchangeCodeForToken(code)

            if (accessToken == null)
            {
                return ResponseEntity.ok(buildErrorPage(
                    "Authorization Failed",
                    "Failed to authorize with Discord. Please try again."
                ))
            }

            // 3. Fetch Discord user info
            val discordUser = fetchDiscordUser(accessToken)

            if (discordUser == null)
            {
                return ResponseEntity.ok(buildErrorPage(
                    "Failed to Fetch Profile",
                    "Could not retrieve your Discord profile. Please try again."
                ))
            }

            // 4. Check if Discord account is already linked to another player
            val existingProfile = akersProfileRepository.findByDiscordId(discordUser.id)
            if (existingProfile != null && existingProfile.id != minecraftUuid)
            {
                return ResponseEntity.ok(buildErrorPage(
                    "Account Already Linked",
                    "This Discord account is already linked to another Minecraft account."
                ))
            }

            // 5. Link Discord account to Minecraft player
            val profile = akersProfileRepository.findById(minecraftUuid).orElse(
                AkersProfileDocument(id = minecraftUuid)
            )

            profile.discordId = discordUser.id
            profile.discordUsername = discordUser.username
            profile.linkedAt = System.currentTimeMillis().toString()

            akersProfileRepository.save(profile)

            // 6. Clean up OAuth token from Redis
            redisTemplate.delete(tokenKey)

            return ResponseEntity.ok(buildSuccessPage(discordUser.username))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            return ResponseEntity.ok(buildErrorPage(
                "An Error Occurred",
                "Something went wrong while linking your account. Please try again."
            ))
        }
    }

    /**
     * Exchange authorization code for access token.
     */
    private fun exchangeCodeForToken(code: String): String?
    {
        try
        {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

            val body = LinkedMultiValueMap<String, String>()
            body.add("client_id", clientId)
            body.add("client_secret", clientSecret)
            body.add("grant_type", "authorization_code")
            body.add("code", code)
            body.add("redirect_uri", redirectUri)

            val request = HttpEntity(body, headers)
            val response = restTemplate.postForEntity(DISCORD_TOKEN_URL, request, String::class.java)

            if (response.statusCode.is2xxSuccessful && response.body != null)
            {
                val json = gson.fromJson(response.body, JsonObject::class.java)
                return json.get("access_token")?.asString
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Fetch Discord user info using access token.
     */
    private fun fetchDiscordUser(accessToken: String): DiscordUser?
    {
        try
        {
            val headers = HttpHeaders()
            headers.setBearerAuth(accessToken)

            val request = HttpEntity<Any>(headers)
            val response = restTemplate.exchange(
                DISCORD_USER_URL,
                HttpMethod.GET,
                request,
                String::class.java
            )

            if (response.statusCode.is2xxSuccessful && response.body != null)
            {
                val json = gson.fromJson(response.body, JsonObject::class.java)
                return DiscordUser(
                    id = json.get("id")?.asString ?: return null,
                    username = json.get("username")?.asString ?: "Unknown",
                    discriminator = json.get("discriminator")?.asString,
                    globalName = json.get("global_name")?.asString
                )
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return null
    }

    private fun buildSuccessPage(discordUsername: String): String
    {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <title>Discord Linked - Arch MC</title>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="description" content="Discord account linking for Arch MC API">
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body {
                        font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(180deg, #0d0d12 0%, #1a1a24 100%);
                        min-height: 100vh;
                        color: #fff;
                    }
                    
                    /* Navbar */
                    .navbar {
                        background: rgba(15, 15, 20, 0.95);
                        border-bottom: 1px solid rgba(255, 255, 255, 0.06);
                        padding: 0 24px;
                        height: 60px;
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        backdrop-filter: blur(20px);
                        position: fixed;
                        top: 0;
                        left: 0;
                        right: 0;
                        z-index: 1000;
                    }
                    .navbar-brand {
                        display: flex;
                        align-items: center;
                        gap: 10px;
                        text-decoration: none;
                    }
                    .navbar-brand .primary-text {
                        color: #8b5cf6;
                        font-weight: 700;
                        font-size: 22px;
                    }
                    .navbar-brand .secondary-text {
                        color: #a0a0b0;
                        font-weight: 500;
                        font-size: 16px;
                    }
                    .navbar-links {
                        display: flex;
                        gap: 32px;
                    }
                    .navbar-links a {
                        color: #a0a0b0;
                        text-decoration: none;
                        font-size: 14px;
                        font-weight: 500;
                        transition: color 0.2s;
                    }
                    .navbar-links a:hover { color: #fff; }
                    
                    /* Main Content */
                    .main-wrapper {
                        padding-top: 60px;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .container {
                        max-width: 480px;
                        width: 100%;
                        padding: 20px;
                    }
                    .card {
                        background: rgba(26, 26, 36, 0.8);
                        border: 1px solid rgba(255, 255, 255, 0.06);
                        border-radius: 16px;
                        padding: 48px 40px;
                        text-align: center;
                        backdrop-filter: blur(20px);
                    }
                    .success-icon {
                        width: 80px;
                        height: 80px;
                        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        margin: 0 auto 24px;
                        font-size: 36px;
                        color: #fff;
                        animation: pop 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
                        box-shadow: 0 8px 24px rgba(16, 185, 129, 0.3);
                    }
                    @keyframes pop {
                        0% { transform: scale(0); }
                        100% { transform: scale(1); }
                    }
                    h1 {
                        color: #10b981;
                        font-size: 26px;
                        font-weight: 700;
                        margin-bottom: 8px;
                    }
                    .discord-user {
                        display: inline-flex;
                        align-items: center;
                        gap: 8px;
                        background: rgba(88, 101, 242, 0.15);
                        border: 1px solid rgba(88, 101, 242, 0.3);
                        color: #5865F2;
                        padding: 8px 16px;
                        border-radius: 8px;
                        font-weight: 600;
                        font-size: 15px;
                        margin: 16px 0 24px;
                    }
                    .discord-user i { font-size: 18px; }
                    p {
                        color: #71717a;
                        font-size: 15px;
                        line-height: 1.6;
                    }
                    .info-box {
                        margin-top: 28px;
                        padding: 16px 20px;
                        background: rgba(139, 92, 246, 0.08);
                        border: 1px solid rgba(139, 92, 246, 0.15);
                        border-radius: 10px;
                    }
                    .info-box p {
                        color: #a78bfa;
                        margin: 0;
                        font-size: 14px;
                    }
                    .info-box i {
                        margin-right: 8px;
                    }
                    
                    /* Footer */
                    .footer {
                        position: fixed;
                        bottom: 0;
                        left: 0;
                        right: 0;
                        background: rgba(15, 15, 20, 0.9);
                        border-top: 1px solid rgba(255, 255, 255, 0.06);
                        padding: 16px 24px;
                        text-align: center;
                        backdrop-filter: blur(20px);
                    }
                    .footer p {
                        color: #52525b;
                        font-size: 13px;
                    }
                    
                    @media (max-width: 640px) {
                        .navbar-links { display: none; }
                        .card { padding: 32px 24px; }
                    }
                </style>
            </head>
            <body>
                <nav class="navbar">
                    <a class="navbar-brand" href="https://arch.mc">
                        <span class="primary-text">Arch</span>
                        <span class="secondary-text">Network</span>
                    </a>
                    <div class="navbar-links">
                        <a href="https://arch.mc"><i class="fa fa-home"></i> Home</a>
                        <a href="https://store.arch.mc"><i class="fa fa-shopping-cart"></i> Store</a>
                        <a href="https://discord.gg/archmc"><i class="fab fa-discord"></i> Discord</a>
                    </div>
                </nav>
                
                <div class="main-wrapper">
                    <div class="container">
                        <div class="card">
                            <div class="success-icon">
                                <i class="fa fa-check"></i>
                            </div>
                            <h1>Discord Linked!</h1>
                            <div class="discord-user">
                                <i class="fab fa-discord"></i>
                                $discordUsername
                            </div>
                            <p>Your Discord account has been successfully linked to your Minecraft account.</p>
                            <div class="info-box">
                                <p><i class="fa fa-gamepad"></i>You can now close this window and return to Minecraft to create your API keys!</p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="footer">
                    <p>Copyright © Arch Network 2024-2025</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildErrorPage(title: String, message: String): String
    {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <title>Error - Arch MC</title>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="description" content="Discord account linking for Arch MC API">
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body {
                        font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(180deg, #0d0d12 0%, #1a1a24 100%);
                        min-height: 100vh;
                        color: #fff;
                    }
                    
                    /* Navbar */
                    .navbar {
                        background: rgba(15, 15, 20, 0.95);
                        border-bottom: 1px solid rgba(255, 255, 255, 0.06);
                        padding: 0 24px;
                        height: 60px;
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        backdrop-filter: blur(20px);
                        position: fixed;
                        top: 0;
                        left: 0;
                        right: 0;
                        z-index: 1000;
                    }
                    .navbar-brand {
                        display: flex;
                        align-items: center;
                        gap: 10px;
                        text-decoration: none;
                    }
                    .navbar-brand .primary-text {
                        color: #8b5cf6;
                        font-weight: 700;
                        font-size: 22px;
                    }
                    .navbar-brand .secondary-text {
                        color: #a0a0b0;
                        font-weight: 500;
                        font-size: 16px;
                    }
                    .navbar-links {
                        display: flex;
                        gap: 32px;
                    }
                    .navbar-links a {
                        color: #a0a0b0;
                        text-decoration: none;
                        font-size: 14px;
                        font-weight: 500;
                        transition: color 0.2s;
                    }
                    .navbar-links a:hover { color: #fff; }
                    
                    /* Main Content */
                    .main-wrapper {
                        padding-top: 60px;
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .container {
                        max-width: 480px;
                        width: 100%;
                        padding: 20px;
                    }
                    .card {
                        background: rgba(26, 26, 36, 0.8);
                        border: 1px solid rgba(255, 255, 255, 0.06);
                        border-radius: 16px;
                        padding: 48px 40px;
                        text-align: center;
                        backdrop-filter: blur(20px);
                    }
                    .error-icon {
                        width: 80px;
                        height: 80px;
                        background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        margin: 0 auto 24px;
                        font-size: 36px;
                        color: #fff;
                        box-shadow: 0 8px 24px rgba(239, 68, 68, 0.3);
                    }
                    h1 {
                        color: #ef4444;
                        font-size: 24px;
                        font-weight: 700;
                        margin-bottom: 16px;
                    }
                    p {
                        color: #71717a;
                        font-size: 15px;
                        line-height: 1.6;
                    }
                    .retry-btn {
                        display: inline-flex;
                        align-items: center;
                        gap: 8px;
                        margin-top: 28px;
                        padding: 12px 24px;
                        background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
                        color: #fff;
                        text-decoration: none;
                        border-radius: 8px;
                        font-weight: 600;
                        font-size: 14px;
                        transition: transform 0.2s, box-shadow 0.2s;
                        box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
                    }
                    .retry-btn:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 6px 20px rgba(139, 92, 246, 0.4);
                    }
                    
                    /* Footer */
                    .footer {
                        position: fixed;
                        bottom: 0;
                        left: 0;
                        right: 0;
                        background: rgba(15, 15, 20, 0.9);
                        border-top: 1px solid rgba(255, 255, 255, 0.06);
                        padding: 16px 24px;
                        text-align: center;
                        backdrop-filter: blur(20px);
                    }
                    .footer p {
                        color: #52525b;
                        font-size: 13px;
                    }
                    
                    @media (max-width: 640px) {
                        .navbar-links { display: none; }
                        .card { padding: 32px 24px; }
                    }
                </style>
            </head>
            <body>
                <nav class="navbar">
                    <a class="navbar-brand" href="https://arch.mc">
                        <span class="primary-text">Arch</span>
                        <span class="secondary-text">Network</span>
                    </a>
                    <div class="navbar-links">
                        <a href="https://arch.mc"><i class="fa fa-home"></i> Home</a>
                        <a href="https://store.arch.mc"><i class="fa fa-shopping-cart"></i> Store</a>
                        <a href="https://discord.gg/archmc"><i class="fab fa-discord"></i> Discord</a>
                    </div>
                </nav>
                
                <div class="main-wrapper">
                    <div class="container">
                        <div class="card">
                            <div class="error-icon">
                                <i class="fa fa-times"></i>
                            </div>
                            <h1>$title</h1>
                            <p>$message</p>
                            <a href="javascript:history.back()" class="retry-btn">
                                <i class="fa fa-arrow-left"></i>
                                Go Back
                            </a>
                        </div>
                    </div>
                </div>
                
                <div class="footer">
                    <p>Copyright © Arch Network 2024-2025</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * OAuth token data stored in Redis.
     */
    data class OAuthTokenData(
        val token: String,
        val minecraftUuid: String,
        val createdAt: Long,
        val expiresAt: Long
    )

    /**
     * Discord user information.
     */
    data class DiscordUser(
        val id: String,
        val username: String,
        val discriminator: String?,
        val globalName: String?
    )
}
