package com.spotifiejpcastor.controllers

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * 🎵 CONTROLADOR DE AUTENTICACIÓN SPOTIFY
 *
 * Maneja:
 * - Login con Spotify OAuth2
 * - Información del usuario autenticado
 * - Datos de Spotify del usuario
 */
@RestController
@CrossOrigin(origins = ["*"])
class SpotifyAuthController {

    private val webClient = WebClient.builder()
        .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024) }
        .build()

    /**
     * 🏠 Página de inicio - Mostrar enlaces de login
     */
    @GetMapping("/")
    fun home(@AuthenticationPrincipal user: OAuth2User?): ResponseEntity<out Map<String, Any?>?> {
        return if (user != null) {
            ResponseEntity.ok(
                mapOf(
                    "message" to "¡Autenticado con Spotify! ✅",
                    "user" to user.name,
                    "spotify_id" to user.getAttribute<String>("id"),
                    "endpoints" to mapOf(
                        "profile" to "/api/spotify/profile",
                        "playlists" to "/api/spotify/playlists",
                        "top_tracks" to "/api/spotify/top-tracks"
                    ),
                    "timestamp" to LocalDateTime.now()
                )
            )
        } else {
            ResponseEntity.ok(
                mapOf(
                    "message" to "No autenticado ❌",
                    "login_url" to "/oauth2/authorization/spotify",
                    "description" to "Visita /oauth2/authorization/spotify para iniciar sesión con Spotify",
                    "timestamp" to LocalDateTime.now()
                )
            )
        }
    }

    /**
     * 🎵 Obtener perfil del usuario de Spotify
     */
    @GetMapping("/profile")
    fun getUserProfile(
        @AuthenticationPrincipal user: OAuth2User?,
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient?
    ): Mono<ResponseEntity<Map<String, Any>>> {

        if (user == null || authorizedClient == null) {
            return Mono.just(
                ResponseEntity.status(401).body(
                    mapOf(
                        "error" to "No autenticado",
                        "message" to "Debes iniciar sesión con Spotify primero",
                        "login_url" to "/oauth2/authorization/spotify"
                    )
                )
            )
        }

        val accessToken = authorizedClient.accessToken.tokenValue

        return webClient.get()
            .uri("https://api.spotify.com/v1/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { spotifyProfile ->
                ResponseEntity.ok(
                    mapOf(
                        "status" to "✅ SUCCESS",
                        "user_info" to mapOf(
                            "spring_security" to mapOf(
                                "name" to user.name,
                                "authorities" to user.authorities.map { it.authority }
                            ),
                            "spotify_profile" to spotifyProfile
                        ),
                        "access_token_info" to mapOf(
                            "expires_at" to authorizedClient.accessToken.expiresAt,
                            "scopes" to authorizedClient.accessToken.scopes
                        ),
                        "timestamp" to LocalDateTime.now()
                    )
                )
            }
            .onErrorReturn(
                ResponseEntity.status(500).body(
                    mapOf(
                        "error" to "Error al obtener perfil de Spotify",
                        "message" to "No se pudo conectar con la API de Spotify"
                    )
                )
            )
    }

    /**
     * 🎶 Obtener playlists del usuario
     */
    @GetMapping("/playlists")
    fun getUserPlaylists(
        @AuthenticationPrincipal user: OAuth2User?,
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient?
    ): Mono<ResponseEntity<Map<String, Any>>> {

        if (user == null || authorizedClient == null) {
            return Mono.just(
                ResponseEntity.status(401).body(
                    mapOf("error" to "No autenticado")
                )
            )
        }

        val accessToken = authorizedClient.accessToken.tokenValue

        return webClient.get()
            .uri("https://api.spotify.com/v1/me/playlists?limit=20")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { playlists ->
                ResponseEntity.ok(
                    mapOf(
                        "status" to "✅ SUCCESS",
                        "playlists" to playlists,
                        "user" to user.name,
                        "timestamp" to LocalDateTime.now()
                    )
                )
            }
            .onErrorReturn(
                ResponseEntity.status(500).body(
                    mapOf("error" to "Error al obtener playlists")
                )
            )
    }

    /**
     * 🔥 Obtener top tracks del usuario
     */
    @GetMapping("/top-tracks")
    fun getTopTracks(
        @AuthenticationPrincipal user: OAuth2User?,
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient?
    ): Mono<ResponseEntity<Map<String, Any>>> {

        if (user == null || authorizedClient == null) {
            return Mono.just(
                ResponseEntity.status(401).body(
                    mapOf("error" to "No autenticado")
                )
            )
        }

        val accessToken = authorizedClient.accessToken.tokenValue

        return webClient.get()
            .uri("https://api.spotify.com/v1/me/top/tracks?limit=10&time_range=short_term")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { topTracks ->
                ResponseEntity.ok(
                    mapOf(
                        "status" to "✅ SUCCESS",
                        "top_tracks" to topTracks,
                        "user" to user.name,
                        "timestamp" to LocalDateTime.now()
                    )
                )
            }
            .onErrorReturn(
                ResponseEntity.status(500).body(
                    mapOf("error" to "Error al obtener top tracks")
                )
            )
    }

    /**
     * 🚪 Logout - Información sobre cerrar sesión
     */
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "message" to "Para cerrar sesión, ve a:",
                "logout_url" to "/logout",
                "description" to "Spring Security manejará el logout automáticamente"
            )
        )
    }
}