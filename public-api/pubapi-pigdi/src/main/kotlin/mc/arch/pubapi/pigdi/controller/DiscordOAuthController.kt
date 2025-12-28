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
                <title>Discord Linked - ArchMC</title>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="description" content="Discord account linking for Arch MC API">
                <script src="https://cdn.tailwindcss.com"></script>
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
                <script>
                    tailwind.config = {
                        theme: {
                            extend: {
                                colors: {
                                    'arch-dark': '#0a0a0f',
                                    'arch-card': '#111116',
                                    'arch-red': '#ef4444',
                                },
                                animation: {
                                    'fade-in': 'fadeIn 0.4s ease-out',
                                    'fade-in-up': 'fadeInUp 0.5s ease-out',
                                    'scale-in': 'scaleIn 0.4s ease-out',
                                    'spin-slow': 'spin 2s linear infinite',
                                    'check-draw': 'checkDraw 0.6s ease-out 0.2s forwards',
                                    'pop-in': 'popIn 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55)',
                                },
                                keyframes: {
                                    fadeIn: {
                                        '0%': { opacity: '0' },
                                        '100%': { opacity: '1' },
                                    },
                                    fadeInUp: {
                                        '0%': { opacity: '0', transform: 'translateY(16px)' },
                                        '100%': { opacity: '1', transform: 'translateY(0)' },
                                    },
                                    scaleIn: {
                                        '0%': { opacity: '0', transform: 'scale(0.95)' },
                                        '100%': { opacity: '1', transform: 'scale(1)' },
                                    },
                                    spin: {
                                        '0%': { transform: 'rotate(0deg)' },
                                        '100%': { transform: 'rotate(360deg)' },
                                    },
                                    checkDraw: {
                                        '0%': { opacity: '0', transform: 'scale(0)' },
                                        '50%': { opacity: '1', transform: 'scale(1.1)' },
                                        '100%': { opacity: '1', transform: 'scale(1)' },
                                    },
                                    popIn: {
                                        '0%': { opacity: '0', transform: 'scale(0.8)' },
                                        '100%': { opacity: '1', transform: 'scale(1)' },
                                    },
                                },
                            },
                        },
                    }
                </script>
            </head>
            <body class="bg-arch-dark text-neutral-100 min-h-screen font-sans antialiased">
                <div class="min-h-screen flex flex-col items-center justify-center px-8 py-32 md:py-32">
                    <div class="mb-16 animate-fade-in-up">
                        <img src="https://i.imgur.com/u11GbgT.png" alt="Arch MC" class="h-20 w-auto">
                    </div>
                    
                    <div class="w-full max-w-[520px] bg-arch-card rounded-lg p-12 md:p-8 text-center animate-scale-in">
                        <div class="relative w-20 h-20 mx-auto mb-8">
                            <div id="loading-spinner" class="absolute inset-0 w-20 h-20 border-4 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin-slow"></div>
                            <div id="success-check" class="absolute inset-0 w-20 h-20 bg-emerald-500 rounded-full flex items-center justify-center text-4xl text-white opacity-0">
                                <i class="fa fa-check"></i>
                            </div>
                        </div>
                        <h1 id="success-title" class="text-3xl md:text-2xl font-semibold text-neutral-100 mb-4 tracking-tight leading-tight opacity-0">Account Linked</h1>
                        <div id="username-display" class="inline-block px-4 py-2 bg-arch-red/12 border border-arch-red/24 rounded-lg text-arch-red text-sm md:text-base font-medium mb-8 opacity-0">
                            $discordUsername
                        </div>
                        <p id="success-message" class="text-zinc-400 text-base leading-relaxed mb-0 opacity-0">Your Discord account has been successfully linked to your Minecraft account. You can close this window and return to the game.</p>
                    </div>
                    
                    <script>
                        setTimeout(() => {
                            const spinner = document.getElementById('loading-spinner');
                            const check = document.getElementById('success-check');
                            const title = document.getElementById('success-title');
                            const username = document.getElementById('username-display');
                            const message = document.getElementById('success-message');
                            
                            spinner.style.opacity = '0';
                            spinner.style.transition = 'opacity 0.3s ease-out';
                            
                            setTimeout(() => {
                                spinner.style.display = 'none';
                                check.style.opacity = '1';
                                check.classList.add('animate-pop-in');
                                
                                setTimeout(() => {
                                    title.style.opacity = '1';
                                    title.style.transition = 'opacity 0.4s ease-out';
                                    setTimeout(() => {
                                        username.style.opacity = '1';
                                        username.style.transition = 'opacity 0.4s ease-out';
                                        setTimeout(() => {
                                            message.style.opacity = '1';
                                            message.style.transition = 'opacity 0.4s ease-out';
                                        }, 100);
                                    }, 100);
                                }, 300);
                            }, 300);
                        }, 1500);
                    </script>
                    
                    <div class="mt-16 text-center animate-fade-in-up" style="animation-delay: 0.2s;">
                        <p class="text-zinc-600 text-xs">© 2025 Rule Your Own Game, Inc.</p>
                    </div>
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
                <title>Error - ArchMC</title>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="description" content="Discord account linking for Arch MC API">
                <script src="https://cdn.tailwindcss.com"></script>
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
                <script>
                    tailwind.config = {
                        theme: {
                            extend: {
                                colors: {
                                    'arch-dark': '#0a0a0f',
                                    'arch-card': '#111116',
                                    'arch-red': '#ef4444',
                                },
                                animation: {
                                    'fade-in': 'fadeIn 0.4s ease-out',
                                    'fade-in-up': 'fadeInUp 0.5s ease-out',
                                    'scale-in': 'scaleIn 0.4s ease-out',
                                    'shake': 'shake 0.5s ease-out',
                                },
                                keyframes: {
                                    fadeIn: {
                                        '0%': { opacity: '0' },
                                        '100%': { opacity: '1' },
                                    },
                                    fadeInUp: {
                                        '0%': { opacity: '0', transform: 'translateY(16px)' },
                                        '100%': { opacity: '1', transform: 'translateY(0)' },
                                    },
                                    scaleIn: {
                                        '0%': { opacity: '0', transform: 'scale(0.95)' },
                                        '100%': { opacity: '1', transform: 'scale(1)' },
                                    },
                                    shake: {
                                        '0%, 100%': { transform: 'translateX(0)' },
                                        '25%': { transform: 'translateX(-4px)' },
                                        '75%': { transform: 'translateX(4px)' },
                                    },
                                },
                            },
                        },
                    }
                </script>
            </head>
            <body class="bg-arch-dark text-neutral-100 min-h-screen font-sans antialiased">
                <div class="min-h-screen flex flex-col items-center justify-center px-8 py-32 md:py-32">
                    <div class="mb-16 animate-fade-in-up">
                        <img src="https://i.imgur.com/u11GbgT.png" alt="Arch MC" class="h-20 w-auto">
                    </div>
                    
                    <div class="w-full max-w-[520px] bg-arch-card rounded-lg p-12 md:p-8 text-center animate-scale-in">
                        <div class="w-18 h-18 bg-arch-red rounded-lg flex items-center justify-center mx-auto mb-8 text-4xl text-white animate-shake">
                            <i class="fa fa-times"></i>
                        </div>
                        <h1 class="text-3xl md:text-2xl font-semibold text-arch-red mb-6 tracking-tight leading-tight">$title</h1>
                        <p class="text-zinc-400 text-base leading-relaxed mb-8">$message</p>
                        <a href="javascript:history.back()" class="inline-flex items-center gap-2 px-6 py-3 bg-arch-red text-white rounded-lg font-medium hover:bg-red-600 transition-colors duration-200">
                            <i class="fa fa-arrow-left"></i>
                            Go back
                        </a>
                    </div>
                    
                    <div class="mt-16 text-center animate-fade-in-up" style="animation-delay: 0.2s;">
                        <p class="text-zinc-600 text-xs">© 2025 Rule Your Own Game, Inc.</p>
                    </div>
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
