package com.spotifiejpcastor.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * 🎵 CONFIGURACIÓN SPRING SECURITY CON SPOTIFY OAUTH2
 *
 * Permite:
 * - OAuth2 Login con Spotify
 * - Endpoints públicos para pruebas Redis
 * - APIs REST sin CSRF
 */
@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange { exchanges ->
                exchanges
                    // ✅ Endpoints públicos (sin autenticación)
                    .pathMatchers("/api/test/**").permitAll()
                    .pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/", "/index.html", "/favicon.ico").permitAll()

                    // 🎵 Endpoints de autenticación OAuth2
                    .pathMatchers("/oauth2/**", "/login/**").permitAll()

                    // 🔒 Todo lo demás requiere autenticación
                    .anyExchange().authenticated()
            }
            // 🎵 HABILITAR OAUTH2 LOGIN
            .oauth2Login { oauth2 ->
                oauth2
                    .authorizationRequestResolver(null) // Usar por defecto
            }
            // 🚫 Deshabilitar CSRF para APIs REST
            .csrf { it.disable() }
            .build()
    }
}